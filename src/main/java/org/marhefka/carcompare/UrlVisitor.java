package org.marhefka.carcompare;

public interface UrlVisitor<T extends VisitorResult> {
    T visit(String url) throws Exception;
}
