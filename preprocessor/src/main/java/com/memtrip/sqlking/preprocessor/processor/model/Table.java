package com.memtrip.sqlking.preprocessor.processor.model;

import com.memtrip.sqlking.preprocessor.processor.Context;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import java.util.ArrayList;
import java.util.List;

public class Table {
    private Element mElement;
    private String mName;
    private String mPackage;
    private List<Member> mMembers;

    public Element getElement() {
        return mElement;
    }

    public String getName() {
        return mName;
    }

    public String getPackage() {
        return mPackage;
    }

    public List<Member> getMembers() {
        return mMembers;
    }

    public Table(Element element) {
        mElement = element;
        mName = assembleName(element);
        mPackage = assemblePackage(element);
        mMembers = assembleMembers(element);
    }

    private String assembleName(Element element) {
        Name name = element.getSimpleName();
        return name.toString();
    }

    private String assemblePackage(Element element) {
        PackageElement packageElement = Context.getInstance().getElementUtils().getPackageOf(element);
        Name name = packageElement.getQualifiedName();
        return name.toString();
    }

    private List<Member> assembleMembers(Element element) {
        List<Member> members = new ArrayList<>();

        if (element.getEnclosedElements() != null && element.getEnclosedElements().size() > 0) {
            for (Element childElement : element.getEnclosedElements()) {
                if (childElement.getKind().isField()) {
                    members.add(new Member(childElement));
                }
            }
        }

        return members;
    }
}