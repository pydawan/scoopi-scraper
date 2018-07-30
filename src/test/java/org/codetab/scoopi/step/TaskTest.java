package org.codetab.scoopi.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.codetab.scoopi.exception.StepPersistenceException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.model.Log.CAT;
import org.codetab.scoopi.shared.StatService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class TaskTest {

    @Mock
    private Step step;
    @Mock
    private MetricsHelper metricsHelper;
    @Mock
    private StatService statService;
    @InjectMocks
    private Task task;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRun() {
        Counter counter = new Counter();
        Timer timer = Mockito.mock(Timer.class);
        Context context = Mockito.mock(Context.class);
        given(metricsHelper.getTimer(step, "task", "time")).willReturn(timer);
        given(timer.time()).willReturn(context);
        given(metricsHelper.getCounter(task, "system", "error"))
                .willReturn(counter);

        task.run();

        InOrder inOrder = inOrder(step, context);
        inOrder.verify(step).getMarker();
        inOrder.verify(step).getLabel();
        inOrder.verify(step).getStepName();
        inOrder.verify(step).initialize();
        inOrder.verify(step).load();
        inOrder.verify(step).process();
        inOrder.verify(step).store();
        inOrder.verify(step).handover();
        inOrder.verify(context).stop();

        verifyNoMoreInteractions(step, context);
    }

    @Test
    public void testRunThrowsException() {

        Counter counter = new Counter();
        Timer timer = Mockito.mock(Timer.class);
        Context context = Mockito.mock(Context.class);
        given(metricsHelper.getTimer(step, "task", "time")).willReturn(timer);
        given(timer.time()).willReturn(context);
        given(metricsHelper.getCounter(task, "system", "error"))
                .willReturn(counter);

        given(step.getLabel()).willReturn("");
        given(step.initialize()).willThrow(StepRunException.class)
                .willThrow(StepPersistenceException.class)
                .willThrow(IllegalStateException.class);

        task.run();
        verify(statService).log(eq(CAT.ERROR), any(String.class),
                any(String.class), any(StepRunException.class));

        task.run();
        verify(statService).log(eq(CAT.ERROR), any(String.class),
                any(String.class), any(StepPersistenceException.class));

        task.run();
        verify(statService).log(eq(CAT.INTERNAL), any(String.class),
                any(String.class), any(IllegalStateException.class));

        assertThat(counter.getCount()).isEqualTo(3L);
    }

    @Test
    public void testGetStep() {
        task.setStep(step);
        assertThat(task.getStep()).isSameAs(step);
    }
}
