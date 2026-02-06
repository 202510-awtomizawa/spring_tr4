package com.example.todo.entity;

public enum Priority {
  HIGH("\u9ad8", "#ef4444"),
  MEDIUM("\u4e2d", "#f59e0b"),
  LOW("\u4f4e", "#22c55e"),
  NONE("\u306a\u3057", "#9ca3af");

  private final String displayName;
  private final String color;

  Priority(String displayName, String color) {
    this.displayName = displayName;
    this.color = color;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getColor() {
    return color;
  }

  public String getCssClass() {
    return "priority-" + name().toLowerCase();
  }
}
