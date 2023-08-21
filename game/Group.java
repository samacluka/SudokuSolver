package game;

import lombok.Data;

import java.util.*;

@Data
public class Group {

    private Cell[] value;

    private List<Integer> asserted = new ArrayList<>();
    private List<Integer> compliments = new ArrayList<>();
    private Boolean isValid;
    private Integer groupSize;

    public Group(Integer groupSize){
        this.groupSize = groupSize;
        this.value = Arrays.stream(new Cell[this.groupSize]).toArray(Cell[]::new);
        for(int c = Cell.MIN_VALUE; c <= groupSize; c++) {
            this.compliments.add(c);
        }
    }

    public void setCell(Integer i, Cell c) {
        this.value[i] = c;
        if(c.getValue() != null) {
            this.asserted.add(c.getValue());
            this.compliments.remove(c.getValue());
            this.isValid = this.validate();
        }
    }

    public Cell getCell(Integer i){
        return this.value[i];
    }

    public Boolean contains(Integer value){
        return this.asserted.contains(value);
    }

    public Boolean validate () {
        if(this.value.length > groupSize || this.value.length < 1) throw new RuntimeException("BAD GROUPING SIZE");

        for (Cell c : this.value){
            if(c == null || !c.validate()) return Boolean.FALSE;
        }

        return this.numDuplicates() == 0;
    }

    public Group swap2(){
        // Collect all Non Locked Cells in this group
        Cell c;
        List<Integer> nonLocked = new ArrayList<>();
        for(int i = 0; i < this.value.length; i++){
            c = this.value[i];
            if(c != null && !c.isLocked()) nonLocked.add(i);
        }
        if(nonLocked.size() < 2) return this; // if there's less than 2, we can't swap anything

        Collections.shuffle(nonLocked); // shuffle for randomness

        // Get the index of the two that will be swapped
        Integer i1 = nonLocked.get(0);
        Integer i2 = nonLocked.get(1);

        // clone this group so we're not manipulating it directly
        Cell[] g = this.value.clone();

        // cache the grid indexes of the two selected cells
        Integer gi1 = g[i1].getIndex();
        Integer gi2 = g[i2].getIndex();

        // swap the two cells
        Cell tmp = g[i1];
        g[i1] = g[i2];
        g[i2] = tmp;

        // set their position info based on the cached values
        g[i1].setPosition(gi1);
        g[i2].setPosition(gi2);

        this.value = g;

        return this;
    }

    public Integer numDuplicates(){
        List<Integer> seen = new ArrayList<>();
        Integer numDuplicates = 0;

        for (Cell c : this.value){
            if(c == null) continue;
            numDuplicates += seen.contains(c.getValue()) ? 1 : 0;
            seen.add(c.getValue());
        }

        return numDuplicates;
    }

}
