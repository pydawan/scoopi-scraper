package org.codetab.scoopi.dao.jdo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.jdo.JDODataStoreException;

import org.codetab.scoopi.model.Document;
import org.codetab.scoopi.model.Locator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;

public class LocatorDaoIT extends ITBase {

    private LocatorDao dao;

    @Rule
    public ExpectedException testRule = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        dao = new LocatorDao(daoUtil.getPersistenceManagerFactory());

        schemaClasses.add("org.codetab.scoopi.model.Locator");
        daoUtil.deleteSchemaForClasses(schemaClasses);
        daoUtil.createSchemaForClasses(schemaClasses);
    }

    @Test
    public void testStoreLocator() {
        Locator locator = createTestLocator();

        dao.storeLocator(locator);

        List<Locator> actuals = daoUtil.getObjects(Locator.class,
                Lists.newArrayList("detachDocuments"));

        assertThat(actuals.size()).isEqualTo(1);

        Locator actual = actuals.get(0);
        actual.setUrl(locator.getUrl());
        assertThat(actual).isEqualTo(locator);
    }

    @Test
    public void testStoreLocatorShouldThrowException() {
        Locator locator = createTestLocator();

        dao.storeLocator(locator);
        // violate unique constraint
        testRule.expect(JDODataStoreException.class);
        dao.storeLocator(locator);
    }

    @Test
    public void testStoreLocatorNullParams() {
        try {
            dao.storeLocator(null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("locator must not be null");
        }
    }

    @Test
    public void testGetLocatorByNameAndGroup() {
        String name = "acme2";
        String group = "group2";

        Locator locator = createTestLocator();
        Locator locator2 = createTestLocator();
        locator2.setName(name);
        locator2.setGroup(group);
        locator2.setUrl("url2");

        dao.storeLocator(locator);
        dao.storeLocator(locator2);

        Locator actual = dao.getLocator(name, group);
        actual.setUrl(locator2.getUrl());

        assertThat(actual).isEqualTo(locator2);
    }

    @Test
    public void testGetLocatorByNameAndGroupNotFound() {
        String name = "acme2";
        String group = "group2";
        Locator locator = createTestLocator();
        dao.storeLocator(locator);

        Locator actual = dao.getLocator(name, group);

        assertThat(actual).isNull();
    }

    @Test
    public void testGetLocatorByNameAndGroupShouldThrowException()
            throws SQLException {
        String name = "acme";
        String group = "group1";

        daoUtil.dropConstraint(daoUtil.getPersistenceManagerFactory(),
                "locator", "UNIQUE_NAMEGROUP");

        Locator locator = createTestLocator();
        dao.storeLocator(locator);
        dao.storeLocator(locator); // duplicate entry

        testRule.expect(IllegalStateException.class);
        dao.getLocator(name, group);
    }

    @Test
    public void testGetLocatorByNameAndGroupNullParams() {
        try {
            dao.getLocator(null, "group");
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("name must not be null");
        }

        try {
            dao.getLocator("name", null);
            fail("must throw NullPointerException");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("group must not be null");
        }
    }

    @Test
    public void testGetLocatorById() {
        Locator locator = createTestLocator();
        dao.storeLocator(locator);

        Locator actual = dao.getLocator(locator.getId());
        actual.setUrl(locator.getUrl());

        assertThat(actual).isEqualTo(locator);
    }

    private Locator createTestLocator() {
        Document document = objectFactory.createDocument("acme", "url1",
                new Date(), new Date());
        Locator locator = objectFactory.createLocator("acme", "group1", "url1");
        locator.getDocuments().add(document);
        return locator;
    }

}
