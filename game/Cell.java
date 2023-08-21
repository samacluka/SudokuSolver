package game;

import lombok.Data;

@Data
public class Cell {

    public static Integer MIN_VALUE = 1; // Should always be 1

    private Boolean isGiven;
    private Boolean isMarked = Boolean.FALSE;

    private Integer value;

    private Integer index;
    private Integer columnPosition;
    private Integer rowPosition;
    private Integer boxPosition;

    private Integer maxValue;

    public Cell(Integer maxValue, Integer value, Integer index){
        this.value = value == null || value > maxValue || value < Cell.MIN_VALUE ? null : value;
        this.isGiven = this.value != null;
        this.maxValue = maxValue;
        this.setPosition(index);
    }

    public void markValue(Integer value){
        if(this.isGiven.equals(Boolean.TRUE)) return;
        this.value = value;
        this.isMarked = Boolean.TRUE;
    }

    public void setValue(Integer value){
        if(this.isGiven.equals(Boolean.TRUE)) return;
        this.value = value;
    }

    public Integer getValue(){
        return this.value;
    }

    public Boolean isLocked(){
        return this.isGiven || this.isMarked;
    }

    public Boolean validate () {
        // MIN VALUE = 1
        // MAX VALUE = GROUP_SIZE
        return this.value != null && this.value >= 1 && this.value <= this.maxValue;
    }

    public void setPosition(Integer index){
        this.index = index;

        if(index != null) {
            this.columnPosition = index % this.maxValue;
            this.rowPosition = index / this.maxValue;

            Integer boxSize = ((Double) Math.sqrt(this.maxValue)).intValue();

            int x = columnPosition / boxSize;
            int y = rowPosition / boxSize;

            this.boxPosition = (y * boxSize) + x;
        }
        else {
            this.columnPosition = null;
            this.rowPosition = null;
            this.boxPosition = null;
        }
    }

}
