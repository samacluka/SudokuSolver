package game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Group {

    public static Integer GROUP_SIZE = Cell.MAX_VALUE;

    private Cell[] value = new Cell[GROUP_SIZE];

    private List<Integer> asserted = new ArrayList<>();
    private List<Integer> compliments = new ArrayList<>();
    private Boolean isValid;

    public Group(){
        for(int c = Cell.MIN_VALUE; c <= Cell.MAX_VALUE; c++) {
            this.compliments.add(c);
        }
    }

    public Group(Integer[] values) {
        for(int c = Cell.MIN_VALUE; c <= Cell.MAX_VALUE; c++) {
            this.compliments.add(c);
        }

        int i = 0;
        for(Integer v : values) {
            this.setCell(i++, new Cell(v));
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

    public Cell get(Integer i){
        return this.value[i];
    }

    public Boolean contains(Integer value){
        return this.asserted.contains(value);
    }

    public Boolean validate () {
        if(this.value.length > GROUP_SIZE || this.value.length < 1) throw new RuntimeException("BAD GROUPING SIZE");

        boolean valid = Boolean.TRUE;
        for (Cell c : this.value){
            valid = valid && (c == null ? Boolean.FALSE : c.validate());
        }

        return valid && this.numDuplicates() == 0;
    }

    public Group swap2(){
        Cell c1 = null;
        Cell c2 = null;
        Integer i1 = -1;
        Integer i2 = -1;
        List<Integer> seenInts = new ArrayList<>();

        // GET C1 to swap
        do {
            i1 = ThreadLocalRandom.current().nextInt(0, GROUP_SIZE);

            if(!seenInts.contains(i1)) seenInts.add(i1);

            c1 = this.get(i1);
        } while(c1 != null && !c1.isLocked().equals(Boolean.TRUE) && seenInts.size() < GROUP_SIZE);


        seenInts = new ArrayList<>();
        seenInts.add(i1);

        // GET C2 to swap
        do {
            i2 = ThreadLocalRandom.current().nextInt(0, GROUP_SIZE);
            if(Objects.equals(i1, i2)) continue;

            if(!seenInts.contains(i2)) seenInts.add(i2);

            c2 = this.get(i2);
        } while(c2 != null && !c2.isLocked().equals(Boolean.TRUE) && seenInts.size() < GROUP_SIZE);

        if (c1 != null && !c1.isLocked().equals(Boolean.TRUE) && c2 != null && !c2.isLocked().equals(Boolean.TRUE)) {
            Cell[] g = this.value.clone();
            Cell tmp = g[i1];
            g[i1] = g[i2];
            g[i2] = tmp;

            g[i1].setPosition(i2);
            g[i2].setPosition(i1);

            this.value = g;
        }

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
