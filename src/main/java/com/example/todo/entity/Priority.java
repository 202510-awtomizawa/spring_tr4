package com.example.todo.entity;

public enum Priority {
  HIGH("高", "#ef4444"),
  MEDIUM("中", "#f59e0b"),
  LOW("低", "#22c55e");

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
