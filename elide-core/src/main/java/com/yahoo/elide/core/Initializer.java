/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.core;

/**
 * Used to perform any additional initialization required on entity beans which is not
 * possible at time of construction.
 * @param <T> bean type
 */
public interface Initializer<T> {

    /**
     * Initialize an entity bean.
     *
     * @param entity Entity bean to initialize
     */
    public void initialize(T entity);
}
