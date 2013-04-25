package com.firstpeople.paintpad.interfaces;

public interface UndoCommand {
    public void undo();
    public void redo();
    public boolean canUndo();
    public boolean canRedo();
}
