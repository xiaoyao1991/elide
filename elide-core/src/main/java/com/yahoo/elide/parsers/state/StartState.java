/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.parsers.state;

import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.PersistentResource;
import com.yahoo.elide.core.exceptions.InvalidAttributeException;
import com.yahoo.elide.core.exceptions.InvalidCollectionException;
import com.yahoo.elide.parsers.ormParser.RootCollectionLoadEntitiesContext;
import com.yahoo.elide.parsers.ormParser.RootCollectionLoadEntityContext;
import com.yahoo.elide.parsers.ormParser.RootCollectionRelationshipContext;
import com.yahoo.elide.parsers.ormParser.RootCollectionSubCollectionContext;

import java.util.Optional;

/**
 * Initial State.
 */
public class StartState extends BaseState {
    @Override
    public void handle(StateContext state, RootCollectionLoadEntitiesContext ctx) {
        String entityName = ctx.term().getText();
        EntityDictionary dictionary = state.getRequestScope().getDictionary();
        Class<?> entityClass = dictionary.getBinding(entityName);
        if (entityClass == null || !dictionary.isRoot(entityClass)) {
            throw new InvalidCollectionException(entityName);
        }
        state.setState(new CollectionTerminalState(entityClass, Optional.empty(), Optional.empty()));
    }

    @Override
    public void handle(StateContext state, RootCollectionLoadEntityContext ctx) {
        EntityDictionary dictionary = state.getRequestScope().getDictionary();
        String entityName = ctx.entity().term().getText();
        String id = ctx.entity().id().getText();
        Class<?> entityClass = dictionary.getBinding(entityName);
        if (entityClass == null || !dictionary.isRoot(entityClass)) {
            throw new InvalidCollectionException(entityName);
        }

        PersistentResource record = PersistentResource.loadRecord(entityClass, id, state.getRequestScope());
        state.setState(new RecordTerminalState(null, record));
    }

    @Override
    public void handle(StateContext state, RootCollectionSubCollectionContext ctx) {
        EntityDictionary dictionary = state.getRequestScope().getDictionary();
        String entityName = ctx.entity().term().getText();
        String id = ctx.entity().id().getText();

        Class<?> entityClass = dictionary.getBinding(entityName);
        if (entityClass == null || !dictionary.isRoot(entityClass)) {
            throw new InvalidCollectionException(entityName);
        }

        PersistentResource record = PersistentResource.loadRecord(entityClass, id, state.getRequestScope());
        state.setState(new RecordState(record));
    }

    @Override
    public void handle(StateContext state, RootCollectionRelationshipContext ctx) {
        EntityDictionary dictionary = state.getRequestScope().getDictionary();
        String entityName = ctx.entity().term().getText();
        String id = ctx.entity().id().getText();
        Class<?> entityClass = dictionary.getBinding(entityName);
        if (entityClass == null || !dictionary.isRoot(entityClass)) {
            throw new InvalidCollectionException(entityName);
        }

        PersistentResource record = PersistentResource.loadRecord(entityClass, id, state.getRequestScope());

        String relationName = ctx.relationship().term().getText();
        try {
            record.getRelation(relationName);
        } catch (InvalidAttributeException e) {
            throw new InvalidCollectionException(relationName);
        }

        state.setState(new RelationshipTerminalState(record, relationName));
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}
