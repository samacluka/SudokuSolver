package game;

import lombok.Data;
import mark.Mark;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Data
public class Sudoku {
    private Integer groupSize;
    private Integer[] originalGrid;
    private List<Cell> grid;
    private Integer score = null;

    public Sudoku(Integer[] grid) {
        this.groupSize = ((Double) Math.sqrt(grid.length)).intValue();

        if(grid.length != Math.pow(groupSize, 2)) throw new RuntimeException("BAD PUZZLE SIZE");

        this.originalGrid = grid;
        this.grid = toCells(grid);
        Mark.mark(this);
    }

    public Sudoku copyLocked(){
        // Initialize with empty grid
        Sudoku copy = new Sudoku(
            Arrays.stream(new Integer[this.originalGrid.length]).map(x -> x = 0).toArray(Integer[]::new)
        );

        for(Cell c : this.grid){
            copy.setCell(c.getColumnPosition(), c.getRowPosition(), c.isLocked().equals(Boolean.TRUE) ? c : null);
        }
        return copy;
    }

    public Sudoku fillRandom(){
        for(Cell c : this.grid){
            if(c.getValue() == null && !c.isLocked()) {
                c.setValue(
                    ThreadLocalRandom.current().nextInt(Cell.MIN_VALUE, (this.groupSize + 1))
                );
            }
        }
        return this;
    }

    public Sudoku fillBoxes(){
        HashMap<Integer, List<Integer>> comps = new HashMap<>();
        for(int i = 0; i < groupSize; i++){
            List<Integer> c = getBox(i).getCompliments();
            Collections.shuffle(c);
            comps.put(i, c);
        }

        List<Integer> comp;
        for(Cell c : this.grid){
            if(c.getValue() == null && !c.isLocked()) {
                comp = comps.get(c.getBoxPosition());

                if(!comp.isEmpty()) {
                    c.setValue(comp.get(0));
                    comp.remove(0);
                    comps.put(c.getBoxPosition(), comp);
                }
                else {
                    c.setValue( ThreadLocalRandom.current().nextInt(Cell.MIN_VALUE, (this.groupSize + 1)) );
                }
            }
        }

        return this;
    }

    public void display(){
        String resetColour = "\033[0m";
        String redColour = "\033[0;31m";
        String greenColour = "\033[0;32m";

        Integer groupLength = ((Double) Math.sqrt(this.groupSize)).intValue();

        // this.rawGrid.length == (Cell.MAX_VALUE * Cell.MAX_VALUE)
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < this.originalGrid.length; i++){
            Cell c = this.grid.get(i);
            Integer v = c.getValue();

            str.append(" ");
            if(v == null) str.append(" ");
            else if(c.getIsGiven().equals(Boolean.TRUE)) str.append(greenColour).append(v);
            else if(c.getIsMarked().equals(Boolean.TRUE)) str.append(redColour).append(v);
            else str.append(v);
            str.append(resetColour);

            // if end of row
            if((i+1) % this.groupSize == 0) {
                // if end of a group of rows (to insert a line between them)
                if((i+1) % (this.groupSize * groupLength) == 0 && (i+1) % Math.pow(this.groupSize, 2) != 0) {
                    String strCopy = str.toString();

                    System.out.println(str);
                    str = new StringBuilder();

                    // Remove invisible characters from array
                    strCopy = strCopy.replaceAll(Pattern.quote(resetColour), "");
                    strCopy = strCopy.replaceAll(Pattern.quote(redColour), "");
                    strCopy = strCopy.replaceAll(Pattern.quote(greenColour), "");

                    // insert line, and a | character for column group spacing
                    for (int j = 0; j < strCopy.length(); j++) {
                        if (strCopy.charAt(j) == '|') str.append("|");
                        else str.append("-");
                    }
                }

                System.out.println(str);
                str = new StringBuilder();
            }
            else if((i+1) % groupLength == 0) {
                str.append(" |");
            }
        }

        System.out.println(" ");
    }

    public Boolean validate() {
        return getScore() == 0;
    }

    public Integer getScore() {
        int s = 0;

        // All Boxes
        for(int i = 0; i < groupSize; i++){
            s += getBox(i).numDuplicates();
        }

        // All Rows
        for(int i = 0; i < groupSize; i++){
            s += getRow(i).numDuplicates();
        }

        // All Columns
        for(int i = 0; i < groupSize; i++){
            s += getColumn(i).numDuplicates();
        }

        this.score = s;
        return s;
    }

    public List<Cell> getCellList() {
        return this.grid;
    }

    private List<Cell> toCells(Integer[] grid){
        List<Cell> cells = new ArrayList<>();

        int index = 0;
        for(Integer v : grid){
            cells.add(new Cell(groupSize, v, index++));
        }
        return cells;
    }

    private Integer[] toIntArray(){
        Integer[] arr = new Integer[this.grid.size()];
        for(Cell c : this.grid){
            arr[c.getIndex()] = c.getValue();
        }
        return arr;
    }

    /**
     *
     * 0,0 | 1,0 | 2,0
     * ---------------
     * 0,1 | 1,1 | 2,1
     * ---------------
     * 0,2 | 1,2 | 2,2
     *
     * */
    public Group getBox(Integer x, Integer y) {
        Integer boxLength = ((Double) Math.sqrt(this.groupSize)).intValue();

        x = x / boxLength;
        y = y / boxLength;

        return getBox((y * boxLength) + x);
    }

    /**
     *
     * 0 | 1 | 2
     * ----------
     * 3 | 4 | 5
     * ----------
     * 6 | 7 | 8
     *
     * */
    public Group getBox(Integer index) {
        Group group = new Group(this.groupSize);

        Integer boxLength = ((Double) Math.sqrt(this.groupSize)).intValue();

        Integer startColumn = (index % boxLength) * boxLength;
        Integer startRow = (index / boxLength) * boxLength;

        int i = 0;
        for (int x = startColumn; x < (startColumn + boxLength); x++) {
            for (int y = startRow; y < (startRow + boxLength); y++) {
                group.setCell(i++, getCell(x, y));
            }
        }

        return group;
    }

    public void setBox(Integer index, Group group) {
        Integer boxLength = ((Double) Math.sqrt(this.groupSize)).intValue();

        Integer startColumn = (index % boxLength) * boxLength;
        Integer startRow = (index / boxLength) * boxLength;

        int c = 0;
        for (int x = startColumn; x < (startColumn + boxLength); x++) {
            for (int y = startRow; y < (startRow + boxLength); y++) {
                setCell(x, y, group.getCell(c++));
            }
        }
    }

    public Group getRow(Integer index) {
        Group group = new Group(this.groupSize);

        for(int i = 0; i < this.groupSize; i++){
            group.setCell(i, getCell(i, index));
        }

        return group;
    }

    public void setRow(Integer index, Group group) {
        for(int i = 0; i < this.groupSize; i++){
            setCell(i, index, group.getCell(i));
        }
    }

    public Group getColumn(Integer index) {
        Group group = new Group(this.groupSize);

        for (int i = 0; i < this.groupSize; i++) {
            group.setCell(i, getCell(index, i));
        }

        return group;
    }

    public void setColumn(Integer index, Group group) {
        for (int i = 0; i < this.groupSize; i++) {
            setCell(index, i, group.getCell(i));
        }
    }

    /**
     *
     * X is ACROSS
     * Y is DOWN
     *
     * 0,0 | 1,0 | 2,0 || 3,0 | 4,0 | 5,0 || 6,0 | 7,0 | 8,0
     * ----------------||-----------------||------------------
     * 0,1 | 1,1 | 2,1 || 3,1 | 4,1 | 5,1 || 6,1 | 7,1 | 8,1
     * ----------------||-----------------||------------------
     * 0,2 | 1,2 | 2,2 || 3,2 | 4,2 | 5,2 || 6,2 | 7,2 | 8,2
     * ================||=================||==================
     * 0,3 | 1,3 | 2,3 || 3,3 | 4,3 | 5,3 || 6,3 | 7,3 | 8,3
     * ----------------||-----------------||------------------
     * 0,4 | 1,4 | 2,4 || 3,4 | 4,4 | 5,4 || 6,4 | 7,4 | 8,4
     * ----------------||-----------------||------------------
     * 0,5 | 1,5 | 2,5 || 3,5 | 4,5 | 5,5 || 6,5 | 7,5 | 8,5
     * ================||=================||==================
     * 0,6 | 1,6 | 2,6 || 3,6 | 4,6 | 5,6 || 6,6 | 7,6 | 8,6
     * ----------------||-----------------||------------------
     * 0,7 | 1,7 | 2,7 || 3,7 | 4,7 | 5,7 || 6,7 | 7,7 | 8,7
     * ----------------||-----------------||------------------
     * 0,8 | 1,8 | 2,8 || 3,8 | 4,8 | 5,8 || 6,8 | 7,8 | 8,8
     *
     * */
    public Cell getCell(Integer x, Integer y){
        return this.getCell((y * this.groupSize) + x);
    }

    public Cell getCell(Integer index){
        return this.grid.get(index);
    }

    private Cell setCell(Integer x, Integer y, Cell cell){
        return this.setCell((y * this.groupSize) + x, cell);
    }
    public Cell setCell(Integer index, Cell cell){
        Cell curr = getCell(index);
        if(curr != null && curr.isLocked().equals(Boolean.TRUE)) return curr;

        if(cell != null) cell.setPosition(index);
        return this.grid.set(index, cell);
    }

    public Sudoku mutate(Double mutationRate) {
        for(int i = 0; i < this.groupSize; i++) {
            if(ThreadLocalRandom.current().nextInt(0, 101) <= mutationRate * 100) {
                setBox(i, getBox(i).swap2());
            }
        }
        return this;
    }

}
