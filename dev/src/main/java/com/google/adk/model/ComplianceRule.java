package com.google.adk.model;

public class ComplianceRule {
    private String forbiddenWord;
    private String replacement;

    public ComplianceRule(String forbiddenWord, String replacement) {
        this.forbiddenWord = forbiddenWord;
        this.replacement = replacement;
    }

    public String getForbiddenWord() { return forbiddenWord; }
    public String getReplacement() { return replacement; }
}
