package game;

import lombok.Data;

@Data
public class Cell {

    public static Integer MIN_VALUE = 1; // Should always be 1
    public static Integer MAX_VALUE = 9; // Should be squares 9, 16, 25, 36, 49, 64, 81 ...

    private Boolean isGiven;
    private Boolean isMarked = Boolean.FALSE;

    private Integer value;

    private Integer index;
    private Integer columnPosition;
    private Integer rowPosition;
    private Integer boxPosition;

    public Cell(Integer value){
        new Cell(value, null);
    }

    public Cell(Integer value, Integer index){
        this.value = value == null || value > Cell.MAX_VALUE || value < Cell.MIN_VALUE ? null : value;
        this.isGiven = this.value != null;
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
        return this.value != null && this.value >= MIN_VALUE && this.value <= MAX_VALUE;
    }

    public void setPosition(Integer index){
        this.index = index;

        if(index != null) {
            this.columnPosition = index % Group.GROUP_SIZE;
            this.rowPosition = index / Group.GROUP_SIZE;

            Double tmp = Math.sqrt(Cell.MAX_VALUE);
            Integer sqrt = tmp.intValue();

            int x = columnPosition / sqrt;
            int y = rowPosition / sqrt;

            this.boxPosition = (y * sqrt) + x;
        }
        else {
            this.columnPosition = null;
            this.rowPosition = null;
            this.boxPosition = null;
        }
    }

}
