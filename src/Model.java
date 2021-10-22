import java.util.*;

public class Model {
    private final static int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    protected int score;
    protected int maxTile;
    private Stack previousStates = new Stack();
    private Stack previousScores = new Stack();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean canMove() {
        if (getEmptyTiles().size() > 0) return true;
        for (int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles.length - 1; j++) {
                if (gameTiles[i][j].value == gameTiles[i][j + 1].value) return true;
            }
        for (int i = 0; i < gameTiles.length - 1; i++)
            for (int j = 0; j < gameTiles.length; j++) {
                if (gameTiles[i][j].value == gameTiles[i + 1][j].value) return true;
            }
        return false;
    }

    private void addTile() {
        List<Tile> list = getEmptyTiles();
        if (list.size() > 0) {
            Tile tile = list.get((int) (list.size() * Math.random()));
            tile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> tileList = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++)
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == 0) {
                    tileList.add(gameTiles[i][j]);
                }
            }
        return tileList;
    }

    protected void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++)
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean flag = false;
        for (int j = 0; j < tiles.length - 1; j ++)
            for (int i = 0; i < tiles.length - 1; i++) {
                if (tiles[i].value == 0 && tiles[i + 1].value != 0) {
                    tiles[i].value = tiles[i + 1].value;
                    tiles[i + 1].value = 0;
                    flag = true;
                }
            }
        return flag;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean flag = false;
        for (int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i].value == tiles[i + 1].value && tiles[i].value != 0) {
                tiles[i].value = tiles[i].value * 2;
                tiles[i + 1].value = 0;
                score += tiles[i].value;
                if (tiles[i].value > maxTile) {
                    maxTile = tiles[i].value;
                }
                compressTiles(tiles);
                flag = true;
            }
        }
        return flag;
    }

    protected void left() {
        if (isSaveNeeded) {
            saveState(gameTiles);
        }
        boolean flag = false;
        for (int i = 0; i < gameTiles.length; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                flag = true;
            }
        }
        if (flag) addTile();
        isSaveNeeded = true;
    }

    protected void right() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateCounterclockwise(gameTiles);
        gameTiles = rotateCounterclockwise(gameTiles);
    }

    protected void up() {
        saveState(gameTiles);
        gameTiles = rotateCounterclockwise(gameTiles);
        left();
        gameTiles = rotateClockwise(gameTiles);
    }

    protected void down() {
        saveState(gameTiles);
        gameTiles = rotateClockwise(gameTiles);
        left();
        gameTiles = rotateCounterclockwise(gameTiles);
    }

    private Tile[][] rotateClockwise(Tile[][] gameTiles) {
        Tile[][] newGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles.length; j++) {
                newGameTiles[j][gameTiles.length - 1 - i] = gameTiles[i][j];
            }
        return newGameTiles;
    }

    private Tile[][] rotateCounterclockwise(Tile[][] gameTiles) {
        Tile[][] newGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles.length; j++) {
                newGameTiles[gameTiles.length - 1 - j][i] = gameTiles[i][j];
            }
        return newGameTiles;
    }

    private void saveState(Tile[][] gameTiles) {
        Tile[][] newGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles.length; j++) {
                newGameTiles[i][j] = new Tile(gameTiles[i][j].value);
            }
        previousStates.push(newGameTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.empty() && !previousScores.empty()) {
            gameTiles = (Tile[][]) previousStates.pop();
            score = (int) previousScores.pop();
        }
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0: left(); break;
            case 1: right(); break;
            case 2: up(); break;
            case 3: down();
        }
    }

    public boolean hasBoardChanged() {
        Tile[][] previousTiles = (Tile[][]) previousStates.peek();
        int sumPreviousTiles = 0;
        int sumGameTiles = 0;
        for (int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles.length; j++) {
                sumGameTiles += gameTiles[i][j].value;
                sumPreviousTiles += previousTiles[i][j].value;
            }
        if (sumGameTiles != sumPreviousTiles) return true;
        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency;
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        } else {
            moveEfficiency = new MoveEfficiency(-1, 0, move);
        }
        rollback();
        return moveEfficiency;
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::down));
        priorityQueue.peek().getMove().move();
    }
}
