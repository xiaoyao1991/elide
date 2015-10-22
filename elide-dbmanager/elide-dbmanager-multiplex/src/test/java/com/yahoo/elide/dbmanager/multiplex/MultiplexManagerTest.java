/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.dbmanager.multiplex;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.yahoo.elide.core.DatabaseTransaction;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.dbmanager.InMemory.InMemoryDB;
import com.yahoo.elide.example.beans.FirstBean;
import com.yahoo.elide.example.other.OtherBean;

import org.testng.annotations.Test;

/**
 * MultiplexManager tests.
 */
public class MultiplexManagerTest {

    @Test
    public void checkLoading() {
        MultiplexManager db = dbInstance();
        EntityDictionary ed = db.getDictionary();
        assertNotNull(ed.getBinding(FirstBean.class));
        assertNotNull(ed.getBinding(OtherBean.class));
    }

    @Test
    public void testValidCommit() throws Exception {
        MultiplexManager db = dbInstance();
        FirstBean object = new FirstBean();
        object.id = 0;
        object.name = "Test";
        try (DatabaseTransaction t = db.beginTransaction()) {
            assertFalse(t.loadObjects(FirstBean.class).iterator().hasNext());
            t.save(object);
            assertFalse(t.loadObjects(FirstBean.class).iterator().hasNext());
            t.commit();
        }
        try (DatabaseTransaction t = db.beginTransaction()) {
            Iterable<FirstBean> beans = t.loadObjects(FirstBean.class);
            assertNotNull(beans);
            assertTrue(beans.iterator().hasNext());
            FirstBean bean = beans.iterator().next();
            assertTrue(bean.id == 1 && bean.name.equals("Test"));
        }
    }

    private static MultiplexManager dbInstance() {
        EntityDictionary ed = new EntityDictionary();
        InMemoryDB db1 = new InMemoryDB(FirstBean.class.getPackage());
        InMemoryDB db2 = new InMemoryDB(OtherBean.class.getPackage());
        MultiplexManager db = new MultiplexManager(db1, db2);
        db.populateEntityDictionary(ed);
        return db;
    }
}
