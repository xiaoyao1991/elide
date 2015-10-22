/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.dbmanager.multiplex;

import com.yahoo.elide.core.DatabaseManager;
import com.yahoo.elide.core.DatabaseTransaction;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.exceptions.TransactionException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple non-persistent in-memory database.
 */
public class MultiplexManager extends DatabaseManager {

    private final List<DatabaseManager> databaseManagers;
    protected final ConcurrentHashMap<Class<?>, DatabaseManager> databaseManagerMap = new ConcurrentHashMap<>();
    private EntityDictionary dictionary;

    public MultiplexManager(DatabaseManager... databaseManagers) {
        this.databaseManagers = Arrays.asList(databaseManagers);
    }

    /**
     * Multiplex transaction handler.
     */
    public class MultiplexTransaction implements DatabaseTransaction {
        protected final LinkedHashMap<DatabaseManager, DatabaseTransaction> transactions = new LinkedHashMap<>();

        public MultiplexTransaction() {
            // create each subordinate transaction
            for (DatabaseManager databaseManager : databaseManagers) {
                transactions.put(databaseManager, databaseManager.beginTransaction());
            }
        }

        @Override
        public void flush() {
            transactions.values().forEach(DatabaseTransaction::flush);
        }

        @Override
        public void save(Object object) {
            getTransaction(object).save(object);
        }

        @Override
        public void delete(Object object) {
            getTransaction(object).delete(object);
        }

        @Override
        public void commit() {
            transactions.values().forEach(DatabaseTransaction::commit);
            transactions.clear();
        }

        @Override
        public <T> T createObject(Class<T> createObject) {
            return getTransaction(createObject).createObject(createObject);
        }

        @Override
        public <T> T loadObject(Class<T> loadClass, String id) {
            return getTransaction(loadClass).loadObject(loadClass, id);
        }

        @Override
        public <T> Iterable<T> loadObjects(Class<T> loadClass) {
            return getTransaction(loadClass).loadObjects(loadClass);
        }

        @Override
        public void close() throws IOException {

            IOException cause = null;
            for (DatabaseTransaction transaction : transactions.values()) {
                try {
                    transaction.close();
                } catch (IOException | Error | RuntimeException e) {
                    if (cause != null) {
                        cause.addSuppressed(e);
                    } else if (e instanceof IOException) {
                        cause = (IOException) e;
                    } else {
                        cause = new IOException(e);
                    }
                }
            }
            if (cause != null) {
                throw cause;
            }
        }

        private DatabaseTransaction getTransaction(Object object) {
            return getTransaction(object.getClass());
        }

        private <T> DatabaseTransaction getTransaction(Class<T> cls) {
            Class<T> type = (Class<T>) EntityDictionary.lookupEntityClass(cls);
            DatabaseTransaction transaction = transactions.get(databaseManagerMap.get(type));
            if (transaction == null) {
                throw new TransactionException(new IllegalStateException("Unknown type " + type));
            }
            return transaction;
        }
    }

    @Override
    public void populateEntityDictionary(EntityDictionary dictionary) {
        this.dictionary = dictionary;

        for (DatabaseManager databaseManager : databaseManagers) {
            EntityDictionary subordinateDictionary = new EntityDictionary();
            databaseManager.populateEntityDictionary(subordinateDictionary);
            for (Class<?> cls : subordinateDictionary.getBindings()) {
                // route class to this database manager
                this.databaseManagerMap.put(cls, databaseManager);
                // bind to multiplex dictionary
                dictionary.bindEntity(cls);
            }
        }
    }

    @Override
    public DatabaseTransaction beginTransaction() {
        return new MultiplexTransaction();
    }

    public EntityDictionary getDictionary() {
        return dictionary;
    }
}
