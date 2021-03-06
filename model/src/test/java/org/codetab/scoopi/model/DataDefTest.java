package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

public class DataDefTest {

    private DataDef dataDef;

    class Enhanced extends DataDef {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private int dnDetachedState = 1;
        @SuppressWarnings("unused")
        private int dnFlags = 2;
        @SuppressWarnings("unused")
        private int dnStateManager = 3;
    }

    @Before
    public void setUp() throws Exception {
        dataDef = new DataDef();
    }

    @Test
    public void testGetFromDate() {
        Date date = new Date();
        dataDef.setFromDate(date);

        assertThat(dataDef.getFromDate()).isEqualTo(date);
    }

    @Test
    public void testGetToDate() {
        Date date = new Date();
        dataDef.setToDate(date);

        assertThat(dataDef.getToDate()).isEqualTo(date);
    }

    @Test
    public void testGetDataDef() {
        String obj = "test";
        dataDef.setDef(obj);

        assertThat(dataDef.getDef()).isEqualTo(obj);
    }

    @Test
    public void testHashCode() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        int expectedHashT1 = HashCodeBuilder.reflectionHashCode(t1, excludes);
        int expectedHashT2 = HashCodeBuilder.reflectionHashCode(t2, excludes);

        assertThat(t1.hashCode()).isEqualTo(expectedHashT1);
        assertThat(t2.hashCode()).isEqualTo(expectedHashT2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    public void testEqualsForDef() {
        ObjectFactory factory = new ObjectFactory();
        Date fromDate = new Date();
        Date toDate = DateUtils.addDays(fromDate, 1);
        DataDef t1 = factory.createDataDef("def1", fromDate, toDate, "json1");
        t1.setId(1L);
        t1.setDef("test def");

        fromDate = DateUtils.addDays(fromDate, 1);
        toDate = DateUtils.addDays(fromDate, 2);
        DataDef t2 = factory.createDataDef("def1", fromDate, toDate, "json1");
        t2.setId(2L);
        t2.setDef("test def");

        assertThat(t1.equalsForDef(t1)).isTrue();
        assertThat(t1.equalsForDef(null)).isFalse();
        assertThat(t1.equalsForDef("some other class")).isFalse();

        assertThat(t1.equalsForDef(t2)).isTrue();

        t2.setName("def2");
        assertThat(t1.equalsForDef(t2)).isFalse();

        t2.setName("def1");
        t2.setDefJson("json2");
        assertThat(t1.equalsForDef(t2)).isFalse();
    }

    @Test
    public void testEqualsObject() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);
        Enhanced t2 = testObjects.get(1);

        String[] excludes =
                {"id", "dnDetachedState", "dnFlags", "dnStateManager"};
        assertThat(EqualsBuilder.reflectionEquals(t1, t2, excludes)).isTrue();

        assertThat(t1).isEqualTo(t2);
        assertThat(t2).isEqualTo(t1);
    }

    @Test
    public void testToString() {
        List<Enhanced> testObjects = createTestObjects();
        Enhanced t1 = testObjects.get(0);

        String expected =
                new ToStringBuilder(t1, ToStringStyle.MULTI_LINE_STYLE)
                        .append("id", t1.getId()).append("name", t1.getName())
                        .append("fromDate", t1.getFromDate())
                        .append("toDate", t1.getToDate()).toString();
        assertThat(t1.toString()).isEqualTo(expected);
    }

    private List<Enhanced> createTestObjects() {
        Date fromDate = new Date();
        Date toDate = DateUtils.addMonths(fromDate, 1);

        Enhanced t1 = new Enhanced();
        t1.setFromDate(fromDate);
        t1.setToDate(toDate);
        t1.setId(1L);
        t1.setName("x");
        t1.setDefJson("json");

        Enhanced t2 = new Enhanced();

        t2.setFromDate(fromDate);
        t2.setToDate(toDate);
        t2.setId(2L);
        t2.setName("x");
        t2.setDefJson("json");
        t2.dnDetachedState = 11;
        t2.dnFlags = 12;
        t2.dnStateManager = 13;

        List<Enhanced> testObjects = new ArrayList<>();
        testObjects.add(t1);
        testObjects.add(t2);
        return testObjects;
    }
}
