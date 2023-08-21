package mark;

import game.Cell;
import game.Group;
import game.Sudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Mark {

    public static void mark(Sudoku sudoku) {

        boolean marksFound;
        do {
            marksFound = findAndSetMarks(sudoku);
        } while(marksFound);

    }

    private static boolean findAndSetMarks(Sudoku sudoku) {
        List<Cell> grid = sudoku.getCellList();

        HashMap<Integer, List<Boolean>> marks = new HashMap<>();
        for(Cell c : grid){
            marks.put(c.getIndex(), populateList(Boolean.TRUE, sudoku.getGroupSize()));
        }

        for(Cell cell : grid){
            if(cell.getValue() != null){
                Group r = sudoku.getRow(cell.getRowPosition());
                Group c = sudoku.getColumn(cell.getColumnPosition());
                Group b = sudoku.getBox(cell.getColumnPosition(), cell.getRowPosition());

                for(Cell rc : r.getValue()){
                    marks.get(rc.getIndex()).set(cell.getValue() - 1, Boolean.FALSE);
                }

                for(Cell cc : c.getValue()){
                    marks.get(cc.getIndex()).set(cell.getValue() - 1, Boolean.FALSE);
                }

                for(Cell bc : b.getValue()){
                    marks.get(bc.getIndex()).set(cell.getValue() - 1, Boolean.FALSE);
                }
            }
        }

        boolean changesMade = Boolean.FALSE;
        for(Cell cell : grid){
            Integer i = isDetermined(marks.get(cell.getIndex()), cell);
            if(i != null){
                cell.markValue(i + 1);
                changesMade = Boolean.TRUE;
            }
        }

        return changesMade;
    }

    private static Integer isDetermined(List<Boolean> bools, Cell cell){
        if(cell.isLocked().equals(Boolean.TRUE)) return null; // if already marked or is given

        int trueCount = 0;
        int trueIndex = -1;

        for (int i = 0; i < bools.size(); i++) {
            if (bools.get(i).equals(Boolean.TRUE)) {
                trueCount++;
                trueIndex = i;
            }
        }

        if (trueCount == 1) {
            return trueIndex;
        } else {
            return null;
        }
    }

    public static <T> List<T> populateList(T value, int length) {
        List<T> resultList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            resultList.add(value);
        }
        return resultList;
    }

}
