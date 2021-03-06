package org.codetab.scoopi.model.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.codetab.scoopi.defs.IItemDef;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Axis;
import org.codetab.scoopi.model.DataDef;
import org.codetab.scoopi.model.Item;
import org.codetab.scoopi.model.Locator;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.ObjectFactory;
import org.codetab.scoopi.step.process.LocatorGroupFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class LocatorGroupFactoryTest {

    @Mock
    private IItemDef itemDefs;
    @Mock
    private ObjectFactory objectFactory;
    @Mock
    private ErrorLogger errorLogger;

    @InjectMocks
    private LocatorGroupFactory locatorGroupFactory;

    private static ObjectFactory factory;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        factory = new ObjectFactory();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateLocatorGroups() {
        String locatorName = "acme";
        DataDef dataDef = factory.createDataDef("price");

        String url1 = "url1";
        String group1 = "group1";
        Optional<String> linkGroup1 = Optional.of(group1);
        Locator locator1 = factory.createLocator(locatorName, group1, url1);
        LocatorGroup locatorGroup1 = factory.createLocatorGroup(group1);

        String url2 = "url2";
        String group2 = "group2";
        Optional<String> linkGroup2 = Optional.of(group2);
        Locator locator2 = factory.createLocator(locatorName, group2, url2);
        LocatorGroup locatorGroup2 = factory.createLocatorGroup(group2);

        String url3 = "url3";
        Locator locator3 = factory.createLocator(locatorName, group2, url3);

        Axis row1 = factory.createAxis("row", "m1", url1, null, 0, 0);
        Axis fact1 = factory.createAxis("fact", "m1", "f1", null, 0, 0);
        Item item1 = factory.createItem();
        item1.addAxis(row1);
        item1.addAxis(fact1);

        Axis row2 = factory.createAxis("row", "m2", url2, null, 0, 0);
        Axis fact2 = factory.createAxis("fact", "m2", "f2", null, 0, 0);
        Item item2 = factory.createItem();
        item2.addAxis(row2);
        item2.addAxis(fact2);

        Axis row3 = factory.createAxis("row", "m3", url3, null, 0, 0);
        Axis fact3 = factory.createAxis("fact", "m3", "f3", null, 0, 0);
        Item item3 = factory.createItem();
        item3.addAxis(row3);
        item3.addAxis(fact3);

        List<Item> items = Lists.newArrayList(item1, item2, item3);

        given(itemDefs.getLinkGroup(dataDef.getName(), row1.getItemName()))
                .willReturn(linkGroup1);
        given(itemDefs.getLinkGroup(dataDef.getName(), row2.getItemName()))
                .willReturn(linkGroup2);
        given(itemDefs.getLinkGroup(dataDef.getName(), row3.getItemName()))
                .willReturn(linkGroup2);

        given(objectFactory.createLocator(locatorName, group1, url1))
                .willReturn(locator1);
        given(objectFactory.createLocator(locatorName, group2, url2))
                .willReturn(locator2);
        given(objectFactory.createLocator(locatorName, group2, url3))
                .willReturn(locator3);

        given(objectFactory.createLocatorGroup(group1))
                .willReturn(locatorGroup1);
        given(objectFactory.createLocatorGroup(group2))
                .willReturn(locatorGroup2);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef.getName(), items, locatorName);

        assertThat(actual.size()).isEqualTo(2);

        assertThat(actual).contains(locatorGroup1, locatorGroup2);
        assertThat(locatorGroup1.getLocators()).containsExactly(locator1);
        assertThat(locatorGroup2.getLocators()).containsExactly(locator2,
                locator3);
    }

    @Test
    public void testCreateLocatorGroupsLinkGroupNotDefined() {
        String locatorName = "acme";
        DataDef dataDef = factory.createDataDef("price");

        String url1 = "url1";
        Optional<String> linkGroup1 = Optional.empty();

        Axis row1 = factory.createAxis("row", "m1", "v1", null, 0, 0);
        Axis fact1 = factory.createAxis("fact", "m1", url1, null, 0, 0);
        Item item1 = factory.createItem();
        item1.addAxis(row1);
        item1.addAxis(fact1);

        List<Item> itemMigs = Lists.newArrayList(item1);

        given(itemDefs.getLinkGroup(dataDef.getName(), row1.getItemName()))
                .willReturn(linkGroup1);

        List<LocatorGroup> actual = locatorGroupFactory
                .createLocatorGroups(dataDef.getName(), itemMigs, locatorName);

        assertThat(actual.size()).isEqualTo(0);

        verify(errorLogger).log(eq(CAT.ERROR), any(String.class));
    }

}
