/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.endpoints;

import com.yahoo.elide.Elide;
import com.yahoo.elide.parsers.ormBaseVisitor;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.testng.annotations.Test;

/**
 * The type Config resource test.
 */
public class ResourceTest {

    @Test
    public void verifyParseRelationship() {
        new ormBaseVisitor().visit(parse("parent/123/relationships/children"));
    }

    @Test
    public void verifyParseRelation() {
        new ormBaseVisitor().visit(parse("company/123/cities/2/relationships/states/1"));
    }

    @Test(expectedExceptions = ParseCancellationException.class)
    public void parseFailRelationship() {
        new ormBaseVisitor().visit(parse("company/123/relationships"));
    }

    @Test(expectedExceptions = ParseCancellationException.class)
    public void parseFailRelationshipCollection() {
        new ormBaseVisitor().visit(parse("company/relationships"));
    }

    @Test(expectedExceptions = ParseCancellationException.class)
    public void parseFailure() {
        new ormBaseVisitor().visit(parse("company/123|apps/2/links/foo"));
    }

    private static ParseTree parse(String path) {
        return Elide.parse(path);
    }
}
