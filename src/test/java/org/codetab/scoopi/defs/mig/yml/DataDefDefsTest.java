package org.codetab.scoopi.defs.mig.yml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.codetab.scoopi.defs.yml.DataDefDef;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.exception.DataDefNotFoundException;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.Data;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.persistence.DataDefPersistence;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class DataDefDefsTest {

    @Mock
    private DataDefDefs dataDefDefs;
    @Mock
    private DataDefPersistence dataDefPersistence;

    @InjectMocks
    private DataDefDef dataDefDef;

    private static ObjectFactory factory;
    private static ObjectMapper mapper;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        mapper = new ObjectMapper();
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitPersistFalse()
            throws JsonProcessingException, IllegalAccessException {
        Map<String, DataDef> dataDefMap = getTestDataDefMap();
        DataDef dataDefA = dataDefMap.get("defA");
        DataDef dataDefB = dataDefMap.get("defB");

        ArrayList<DataDef> dataDefs = Lists.newArrayList(dataDefA, dataDefB);

        JsonNode defs = mapper.createObjectNode();

        Data dataA = factory.createData("defA");
        Data dataB = factory.createData("defB");

        List<Set<Axis>> axisSetsA = new ArrayList<>();
        List<Set<Axis>> axisSetsB = new ArrayList<>();

        given(dataDefPersistence.persistDataDef()).willReturn(false);
        given(dataDefDefs.createDataDefs(defs)).willReturn(dataDefs);
        given(dataDefDefs.toMap(dataDefs)).willReturn(dataDefMap);

        given(dataDefDefs.getAxisSets(dataDefA)).willReturn(axisSetsA);
        given(dataDefDefs.getAxisSets(dataDefB)).willReturn(axisSetsB);
        given(dataDefDefs.getData(dataDefA, axisSetsA)).willReturn(dataA);
        given(dataDefDefs.getData(dataDefB, axisSetsB)).willReturn(dataB);

        dataDefDef.init(defs);

        @SuppressWarnings("unchecked")
        Map<String, Data> map = (Map<String, Data>) FieldUtils
                .readField(dataDefDef, "dataTemplateMap", true);

        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get(dataDefA.getName())).isEqualTo(dataA);
        assertThat(map.get(dataDefB.getName())).isEqualTo(dataB);
    }

    @Test
    public void testInitPersistTrueNoUpdates()
            throws JsonProcessingException, IllegalAccessException {
        DataDef dataDef1 = factory.createDataDef("def1", new Date(), new Date(),
                "defJson1");
        ArrayList<DataDef> definedDataDefs = Lists.newArrayList(dataDef1);

        Map<String, DataDef> dataDefMap = getTestDataDefMap();
        DataDef dataDefA = dataDefMap.get("defA");
        DataDef dataDefB = dataDefMap.get("defB");

        ArrayList<DataDef> loadedDataDefs =
                Lists.newArrayList(dataDefA, dataDefB);

        JsonNode defs = mapper.createObjectNode();

        Data dataA = factory.createData("defA");
        Data dataB = factory.createData("defB");

        List<Set<Axis>> axisSetsA = new ArrayList<>();
        List<Set<Axis>> axisSetsB = new ArrayList<>();

        given(dataDefPersistence.persistDataDef()).willReturn(true);
        given(dataDefDefs.createDataDefs(defs)).willReturn(definedDataDefs);
        given(dataDefDefs.toMap(loadedDataDefs)).willReturn(dataDefMap);

        given(dataDefPersistence.loadDataDefs()).willReturn(loadedDataDefs);
        given(dataDefDefs.markForUpdation(definedDataDefs, loadedDataDefs))
                .willReturn(false);
        given(dataDefDefs.getAxisSets(dataDefA)).willReturn(axisSetsA);
        given(dataDefDefs.getAxisSets(dataDefB)).willReturn(axisSetsB);
        given(dataDefDefs.getData(dataDefA, axisSetsA)).willReturn(dataA);
        given(dataDefDefs.getData(dataDefB, axisSetsB)).willReturn(dataB);

        dataDefDef.init(defs);

        @SuppressWarnings("unchecked")
        Map<String, Data> map = (Map<String, Data>) FieldUtils
                .readField(dataDefDef, "dataTemplateMap", true);

        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get(dataDefA.getName())).isEqualTo(dataA);
        assertThat(map.get(dataDefB.getName())).isEqualTo(dataB);
    }

    @Test
    public void testInitPersistTrueHasUpdates()
            throws JsonProcessingException, IllegalAccessException {
        DataDef dataDef1 = factory.createDataDef("def1", new Date(), new Date(),
                "defJson1");
        ArrayList<DataDef> definedDataDefs = Lists.newArrayList(dataDef1);

        DataDef dataDef2 = factory.createDataDef("def2", new Date(), new Date(),
                "defJson2");
        ArrayList<DataDef> loadedDataDefs = Lists.newArrayList(dataDef2);

        Map<String, DataDef> dataDefMap = getTestDataDefMap();
        DataDef dataDefA = dataDefMap.get("defA");
        DataDef dataDefB = dataDefMap.get("defB");

        ArrayList<DataDef> updatedDataDefs =
                Lists.newArrayList(dataDefA, dataDefB);

        JsonNode defs = mapper.createObjectNode();

        Data dataA = factory.createData("defA");
        Data dataB = factory.createData("defB");

        List<Set<Axis>> axisSetsA = new ArrayList<>();
        List<Set<Axis>> axisSetsB = new ArrayList<>();

        given(dataDefPersistence.persistDataDef()).willReturn(true);
        given(dataDefDefs.createDataDefs(defs)).willReturn(definedDataDefs);
        given(dataDefPersistence.loadDataDefs()).willReturn(loadedDataDefs)
                .willReturn(updatedDataDefs);
        given(dataDefDefs.markForUpdation(definedDataDefs, loadedDataDefs))
                .willReturn(true);

        given(dataDefDefs.toMap(updatedDataDefs)).willReturn(dataDefMap);

        given(dataDefDefs.getAxisSets(dataDefA)).willReturn(axisSetsA);
        given(dataDefDefs.getAxisSets(dataDefB)).willReturn(axisSetsB);
        given(dataDefDefs.getData(dataDefA, axisSetsA)).willReturn(dataA);
        given(dataDefDefs.getData(dataDefB, axisSetsB)).willReturn(dataB);

        dataDefDef.init(defs);

        verify(dataDefPersistence).storeDataDefs(loadedDataDefs);

        @SuppressWarnings("unchecked")
        Map<String, Data> map = (Map<String, Data>) FieldUtils
                .readField(dataDefDef, "dataTemplateMap", true);

        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get(dataDefA.getName())).isEqualTo(dataA);
        assertThat(map.get(dataDefB.getName())).isEqualTo(dataB);
    }

    @Test
    public void testInitAlreadyInitialized() throws IllegalAccessException {
        FieldUtils.writeField(dataDefDef, "consistent", true, true);

        JsonNode defs = mapper.createObjectNode();
        dataDefDef.init(defs);

        verifyZeroInteractions(dataDefPersistence, dataDefDefs);
    }

    @Test
    public void testInitShouldThrowException() throws JsonProcessingException {
        JsonNode defs = mapper.createObjectNode();

        given(dataDefDefs.createDataDefs(defs))
                .willThrow(JsonProcessingException.class);

        testRule.expect(CriticalException.class);
        dataDefDef.init(defs);
    }

    @Test
    public void testInitNullParams() {
        try {
            dataDefDef.init(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage())
                    .isEqualTo("dataDefDefs must not be null");
        }
    }

    @Test
    public void testGetDataDef()
            throws IllegalAccessException, DataDefNotFoundException {
        Map<String, DataDef> dataDefMap = getTestDataDefMap();
        DataDef dataDefA = dataDefMap.get("defA");
        DataDef dataDefB = dataDefMap.get("defB");

        FieldUtils.writeField(dataDefDef, "dataDefMap", dataDefMap, true);

        assertThat(dataDefDef.getDataDef("defA")).isEqualTo(dataDefA);
        assertThat(dataDefDef.getDataDef("defB")).isEqualTo(dataDefB);

        testRule.expect(DataDefNotFoundException.class);
        dataDefDef.getDataDef("defC");
    }

    @Test
    public void testGetDataTemplate() throws IllegalAccessException {
        Data dataA = factory.createData("defA");

        Map<String, Data> dataTemplateMap = new HashMap<>();
        dataTemplateMap.put("def1", dataA);

        FieldUtils.writeField(dataDefDef, "dataTemplateMap", dataTemplateMap,
                true);

        Data actual = dataDefDef.getDataTemplate("def1");

        assertThat(actual).isEqualTo(dataA);

        testRule.expect(NoSuchElementException.class);
        dataDefDef.getDataTemplate("def2");
    }

    public Map<String, DataDef> getTestDataDefMap() {
        DataDef dataDefA = factory.createDataDef("defA", new Date(), new Date(),
                "defJsonA");
        DataDef dataDefB = factory.createDataDef("defB", new Date(), new Date(),
                "defJsonB");

        Map<String, DataDef> dataDefMap = new HashMap<>();
        dataDefMap.put(dataDefA.getName(), dataDefA);
        dataDefMap.put(dataDefB.getName(), dataDefB);
        return dataDefMap;
    }

}