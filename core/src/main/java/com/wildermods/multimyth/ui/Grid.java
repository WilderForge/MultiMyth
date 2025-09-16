package com.wildermods.multimyth.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;

public class Grid extends WidgetGroup {
    private int cellSize;
    private Array<Actor> children;
    private float padding = 0;
    private float spacing = 0;
    private float lastWidth = 0;
    
    public Grid(int cellSize) {
        this.cellSize = cellSize;
        this.children = new Array<>();
    }
    
    public Grid(int cellSize, float padding, float spacing) {
        this(cellSize);
        this.padding = padding;
        this.spacing = spacing;
    }
    
    @Override
    public void addActor(Actor actor) {
        super.addActor(actor);
        children.add(actor);
        invalidateHierarchy();
    }
    
    @Override
    public boolean removeActor(Actor actor) {
        boolean result = super.removeActor(actor);
        children.removeValue(actor, true);
        invalidateHierarchy();
        return result;
    }
    
    @Override
    public void clearChildren() {
        super.clearChildren();
        children.clear();
        invalidateHierarchy();
    }
    
    @Override
    public void layout() {
        float width = getWidth();
        
        // Only update layout if width has changed
        if (width != lastWidth) {
            lastWidth = width;
            
            // Calculate available width after accounting for padding
            float availableWidth = width - (padding * 2);
            
            // Calculate number of columns that can fit
            int cols = Math.max(1, (int) ((availableWidth + spacing) / (cellSize + spacing)));
            
            // Calculate required height based on number of rows
            int rows = (int) Math.ceil((float) children.size / cols);
            float requiredHeight = (padding * 2) + (rows * (cellSize + spacing)) - spacing;
            
            // Set our height to the required height
            setHeight(requiredHeight);
            
            // Position children in grid (top-aligned)
            for (int i = 0; i < children.size; i++) {
                Actor child = children.get(i);
                int row = i / cols;
                int col = i % cols;
                
                float x = padding + col * (cellSize + spacing);
                // Top alignment: position from the top of the container
                float y = getHeight() - padding - (row + 1) * (cellSize + spacing);
                
                child.setBounds(x, y, cellSize, cellSize);
            }
        }
    }
    
    @Override
    public float getPrefWidth() {
        return getWidth(); // Take whatever width is available
    }
    
    @Override
    public float getPrefHeight() {
        float width = getWidth();
        float availableWidth = width - (padding * 2);
        int cols = Math.max(1, (int) ((availableWidth + spacing) / (cellSize + spacing)));
        int rows = (int) Math.ceil((float) children.size / cols);
        return (padding * 2) + (rows * (cellSize + spacing)) - spacing;
    }
    
    @Override
    public float getMinWidth() {
        return cellSize + (padding * 2); // Minimum width is one cell plus padding
    }
    
    @Override
    public float getMinHeight() {
        return cellSize + (padding * 2); // Minimum height is one cell plus padding
    }
    
    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
        lastWidth = 0; // Reset to force layout update
        invalidateHierarchy();
    }
    
    public int getCellSize() {
        return cellSize;
    }
    
    public void setPadding(float padding) {
        this.padding = padding;
        lastWidth = 0; // Reset to force layout update
        invalidateHierarchy();
    }
    
    public float getPadding() {
        return padding;
    }
    
    public void setSpacing(float spacing) {
        this.spacing = spacing;
        lastWidth = 0; // Reset to force layout update
        invalidateHierarchy();
    }
    
    public float getSpacing() {
        return spacing;
    }
}