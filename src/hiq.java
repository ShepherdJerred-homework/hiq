// hiq
// Jerred Shepherd

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class hiq {
    public static void main(String[] args) throws FileNotFoundException {
        List<Input> inputs = getInputs();
        List<Output> outputs = solveInputs(inputs);
        printOutputs(outputs);
    }

    static void printOutputs(List<Output> outputs) throws FileNotFoundException {
        File outputFile = new File("hiq.out");
        PrintWriter printWriter = new PrintWriter(outputFile);
        outputs.forEach(output -> {
            System.out.println(output.toFormattedString());
            printWriter.println(output.toFormattedString());
        });
        printWriter.close();
    }

//    static String solveDFS(Board board, String s, HashSet<Board> states) {
//        if (board.isPegInInitialLocation()) {
//            return s;
//        }
//        Set<Move> moves = board.findPossibleMoves();
//        for (Move move : moves) {
//            Board newBoard = new Board(board);
//            newBoard.doMove(move);
//            if (!states.contains(newBoard)) {
//                states.add(newBoard);
//                String result = solveDFS(newBoard, s + String.format("%s->%s ", move.from, move.to), states);
//                if (result != null) {
//                    return result;
//                }
//            } else {
////                System.out.println("Pruned");
//            }
//        }
//        return null;
//    }
//
//    static Output solveInputDFS(Input input) {
//        Board initialBoard = new Board(7, 7, input.startingPosition);
//        String solution = solveDFS(initialBoard, "", new HashSet<>());
//        System.out.println(solution);
//        return new Output(solution);
//    }

    static Output solveInputBFS(Input input) {
        Board initialBoard = new Board(7, 7, input.startingPosition);
        System.out.println(initialBoard.cellsToString());

        Set<Board> set = new HashSet<>();
        Queue<QueueEntry> queue = new LinkedList<>();

        initialBoard.findPossibleMoves().forEach(move -> {
            QueueEntry entry = new QueueEntry(new ArrayList<>(), move, new Board(initialBoard));
            queue.add(entry);
        });
        set.add(initialBoard);

        int lastSeenLow = 33;
        List<Move> solution = null;
        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
//            System.out.println(entry);
            Board board = new Board(entry.board);
//            System.out.println("BEFORE MOVE");
//            System.out.println(entry.nextMove);
//            System.out.println(board.cellsToString());
            board.doMove(entry.nextMove);
//            System.out.println("AFTER MOVE");
//            System.out.println(board.numberOfPegs);
//            System.out.println(board.cellsToString());
            List<Move> moves = new ArrayList<>(entry.previousMoves);
            moves.add(entry.nextMove);
            if (entry.board.numberOfPegs < lastSeenLow) {
                System.out.println("Number of pegs left: " + entry.board.numberOfPegs);
                lastSeenLow = entry.board.numberOfPegs;
                System.out.println("Entries in queue: " + queue.size());
            }
            if (board.isPegInInitialLocation()) {
                solution = moves;
                break;
            } else {
                if (set.contains(board)) {
//                System.out.println("Pruned");
                    continue;
                } else {
                    set.add(board);
                }
                board.findPossibleMoves().forEach(move -> {
                    QueueEntry newEntry = new QueueEntry(moves, move, board);
                    queue.add(newEntry);
                });
            }
        }

        System.out.println(solution);
        return new Output(input.startingPosition, solution);
    }

    static List<Output> solveInputs(List<Input> inputs) {
        List<Output> outputs = new ArrayList<>();
        inputs.forEach(input -> outputs.add(solveInputBFS(input)));
        return outputs;
    }

    static List<Input> getInputs() throws FileNotFoundException {
        File file = new File("hiq.in");
        Scanner scanner = new Scanner(file);
        List<Input> inputs = new ArrayList<>();
        while (scanner.hasNext()) {
            char c = scanner.next().charAt(0);
            if (c == '0') {
                break;
            } else {
                Input input = new Input(c);
                inputs.add(input);
            }
        }
        return inputs;
    }

    static class QueueEntry {
        final List<Move> previousMoves;
        final Move nextMove;
        final Board board;

        QueueEntry(List<Move> previousMoves, Move nextMove, Board board) {
            this.previousMoves = previousMoves;
            this.nextMove = nextMove;
            this.board = board;
        }

        @Override
        public String toString() {
            return "QueueEntry{" +
                    "previousMoves=" + previousMoves +
                    ", nextMove=" + nextMove +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QueueEntry entry = (QueueEntry) o;
            return Objects.equals(nextMove, entry.nextMove) &&
                    Objects.equals(board, entry.board);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nextMove, board);
        }
    }

    static class Move {
        final Coordinate from;
        final Coordinate to;
        final Coordinate over;

        Move(Coordinate from, Coordinate to, Coordinate over) {
            this.from = from;
            this.to = to;
            this.over = over;
        }

        @Override
        public String toString() {
            return "Move{" +
                    "from=" + from +
                    ", to=" + to +
                    ", over=" + over +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return Objects.equals(from, move.from) &&
                    Objects.equals(to, move.to) &&
                    Objects.equals(over, move.over);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, over);
        }
    }

    static class CellStateContainer {
        final Board.CellState[][] cells;

        CellStateContainer(Board.CellState[][] cells) {
            this.cells = cells;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CellStateContainer that = (CellStateContainer) o;
            return Arrays.equals(cells, that.cells);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(cells);
        }
    }

    static class Board {
        final int rows;
        final int columns;
        final CellState[][] cells;
        final Map<Character, Coordinate> characterCoordinateMap;
        final Map<Coordinate, Character> coordinateCharacterMap;
        int numberOfPegs;
        final char emptySpace;

        enum CellState {
            FILLED, EMPTY, UNUSED;

            @Override
            public String toString() {
                switch (this) {
                    case FILLED:
                        return "P";
                    case EMPTY:
                        return " ";
                    case UNUSED:
                        return "X";
                    default:
                        return "?";
                }
            }
        }

        Board(Board board) {
            this.rows = board.rows;
            this.columns = board.columns;
            this.characterCoordinateMap = board.characterCoordinateMap;
            this.coordinateCharacterMap = board.coordinateCharacterMap;
            this.numberOfPegs = board.numberOfPegs;
            this.emptySpace = board.emptySpace;

            this.cells = new CellState[rows][columns];
            for (int row = 0; row < rows; row++) {
                if (columns >= 0) System.arraycopy(board.cells[row], 0, this.cells[row], 0, columns);
            }
        }

        Board(int rows, int columns, char emptySpace) {
            this.rows = rows;
            this.columns = columns;
            cells = new CellState[rows][columns];
            this.numberOfPegs = 0;
            this.emptySpace = emptySpace;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    Coordinate coordinate = new Coordinate(row, col);
                    CellState state = CellState.FILLED;

                    // Top left
                    if (row < 2 && col < 2) {
                        state = CellState.UNUSED;
                    }
                    // Top right
                    if (row < 2 && col > 4) {
                        state = CellState.UNUSED;
                    }
                    // Bottom left
                    if (row > 4 && col < 2) {
                        state = CellState.UNUSED;
                    }
                    // Bottom right
                    if (row > 4 && col > 4) {
                        state = CellState.UNUSED;
                    }

                    if (state == CellState.FILLED) {
                        numberOfPegs++;
                    }

                    setCell(coordinate, state);
                }
            }

            characterCoordinateMap = new HashMap<>();
            coordinateCharacterMap = new HashMap<>();
            char nextChar = '1';
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    Coordinate coordinate = new Coordinate(row, col);
                    if (!isUnused(coordinate)) {
                        characterCoordinateMap.put(nextChar, coordinate);
                        coordinateCharacterMap.put(coordinate, nextChar);
                        if (nextChar == '9') {
                            nextChar = 'A';
                        } else if (nextChar == 'N') {
                            nextChar = 'P';
                        } else {
                            nextChar++;
                        }
                    }
                }
            }

            setCell(characterCoordinateMap.get(emptySpace), CellState.EMPTY);
        }

        private boolean isPegInInitialLocation() {
            Coordinate coordinateToCheck = characterCoordinateMap.get(emptySpace);
            return this.numberOfPegs == 1 && this.cells[coordinateToCheck.row][coordinateToCheck.col] == CellState.FILLED;
        }

        private void doMove(Move move) {
            setCell(move.from, CellState.EMPTY);
            setCell(move.over, CellState.EMPTY);
            setCell(move.to, CellState.FILLED);
            numberOfPegs--;
        }

        private boolean areValidCoordinates(Coordinate... coordinates) {
            for (Coordinate coordinate : coordinates) {
                if (!isValidCoordinate(coordinate)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isValidCoordinate(Coordinate coordinate) {
            if (coordinate.row > 0 && coordinate.row < 6 && coordinate.col > 0 && coordinate.col < 6) {
                return !isUnused(coordinate);
            }
            return false;
        }

        public boolean areCoordinatesValidAndFilled(Coordinate... coordinates) {
            return areValidCoordinates(coordinates) && areFilled(coordinates);
        }

        public Set<Move> findPossibleMoves() {
            Set<Move> possibleMoves = new HashSet<>();
            List<Coordinate> emptyCells = findEmptyCells();
            emptyCells.forEach(cell -> {
                Coordinate oneAboveCell = new Coordinate(cell.row, cell.col - 1);
                Coordinate twoAboveCell = new Coordinate(cell.row, cell.col - 2);
                if (areCoordinatesValidAndFilled(oneAboveCell, twoAboveCell)) {
                    Move move = new Move(twoAboveCell, cell, oneAboveCell);
                    possibleMoves.add(move);
//                    System.out.println(move);
                }

                Coordinate oneBelowCell = new Coordinate(cell.row, cell.col + 1);
                Coordinate twoBelowCell = new Coordinate(cell.row, cell.col + 2);
                if (areCoordinatesValidAndFilled(oneBelowCell, twoBelowCell)) {
                    Move move = new Move(twoBelowCell, cell, oneBelowCell);
                    possibleMoves.add(move);
//                    System.out.println(move);
                }

                Coordinate oneLeftOfCell = new Coordinate(cell.row - 1, cell.col);
                Coordinate twoLeftOfCell = new Coordinate(cell.row - 2, cell.col);
                if (areCoordinatesValidAndFilled(oneLeftOfCell, twoLeftOfCell)) {
                    Move move = new Move(twoLeftOfCell, cell, oneLeftOfCell);
                    possibleMoves.add(move);
//                    System.out.println(move);
                }

                Coordinate oneRightOfCell = new Coordinate(cell.row + 1, cell.col);
                Coordinate twoRightOfCell = new Coordinate(cell.row + 2, cell.col);
                if (areCoordinatesValidAndFilled(oneRightOfCell, twoRightOfCell)) {
                    Move move = new Move(twoRightOfCell, cell, oneRightOfCell);
                    possibleMoves.add(move);
//                    System.out.println(move);
                }
            });
            return possibleMoves;
        }

        public List<Coordinate> findEmptyCells() {
            List<Coordinate> coordinates = new ArrayList<>();
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    Coordinate coordinate = new Coordinate(row, col);
                    if (isEmpty(coordinate)) {
                        coordinates.add(coordinate);
                    }
                }
            }
            return coordinates;
        }

        private boolean areFilled(Coordinate... coordinates) {
            for (Coordinate coordinate : coordinates) {
                if (!isFilled(coordinate)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isFilled(Coordinate coordinate) {
            return isOfType(coordinate, CellState.FILLED);
        }

        private boolean isEmpty(Coordinate coordinate) {
            return isOfType(coordinate, CellState.EMPTY);
        }

        private boolean isUnused(Coordinate coordinate) {
            return isOfType(coordinate, CellState.UNUSED);
        }

        private boolean isOfType(Coordinate coordinate, CellState state) {
            return cells[coordinate.row][coordinate.col] == state;
        }

        private void setCell(Coordinate coordinate, CellState newState) {
            cells[coordinate.row][coordinate.col] = newState;
        }

        public String cellsToString() {
            StringBuilder sb = new StringBuilder();
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    sb.append(cells[row][col]);
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "Board{" +
                    "rows=" + rows +
                    ", columns=" + columns +
                    ", cells=" + Arrays.deepToString(cells) +
                    ", characterCoordinateMap=" + characterCoordinateMap +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Board board = (Board) o;
            return rows == board.rows &&
                    columns == board.columns &&
                    numberOfPegs == board.numberOfPegs &&
                    emptySpace == board.emptySpace &&
                    Arrays.deepEquals(cells, board.cells);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(rows, columns, numberOfPegs, emptySpace);
            result = 31 * result + Arrays.deepHashCode(cells);
            return result;
        }
    }

    static class Coordinate {
        int row;
        int col;

        public Coordinate(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Coordinate that = (Coordinate) o;
            return row == that.row &&
                    col == that.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return "Coordinate{" +
                    "row=" + row +
                    ", col=" + col +
                    '}';
        }
    }
//
//    static class Output {
//        final String result;
//
//        Output(String result) {
//            this.result = result;
//        }
//
//        String toFormattedString() {
//            return result;
//        }
//    }

    static class Output {
        final char c;
        final List<Move> moves;

        Output(final char c, List<Move> moves) {
            this.c = c;
            this.moves = moves;
        }

        String toFormattedString() {
            if (moves == null) {
                return "No Solution for hole at " + c;
            } else {
                return moves.toString();
            }
        }
    }

    static class Input {
        char startingPosition;

        public Input(char startingPosition) {
            this.startingPosition = startingPosition;
        }

        @Override
        public String toString() {
            return "Input{" +
                    "startingPosition=" + startingPosition +
                    '}';
        }
    }
}
