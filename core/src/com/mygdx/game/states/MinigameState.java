package com.mygdx.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.misc.Button;
import com.mygdx.game.sprites.Pipe;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.utils.Timer;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class MinigameState extends State {
    Button giveup;
    Pipe[][] positions;
    Boolean finished;
    int[] rotations = new int[] {0, 90, 180, 270};
    public int test;
    Texture minigameComplete;

    /**
     * Constructor to initialise the minigame
     *
     * @param gameStateManager the class containing the stack of States
     */
    protected MinigameState(GameStateManager gameStateManager) {
        super(gameStateManager);
        Random rand = new Random();
        positions = new Pipe[6][4];
        int startPos = rand.nextInt(3);
        positions[0][startPos] = new Pipe(new Vector2(500,400 + startPos * 100), 100, 100, new Texture("StartPipe.png"), 0, new int[] {0});
        int endPos = rand.nextInt(3);
        positions[5][endPos] = new Pipe(new Vector2(1000,400 + endPos * 100), 100, 100, new Texture("StartPipe.png"), 180, new int[] {180});
        findPath(new Vector2(1, startPos), new Vector2(5, endPos), new Vector2(0, startPos));
        giveup = new Button(new Texture("giveup.png"), new Texture ("giveup.png"), 190, 49, new Vector2(750,200),false,false);
        minigameComplete = new Texture("MinigameComplete.png");
        finished = false;
        test = 0;
    }

    @Override
    public void update(float deltaTime) {
        if (giveup.mouseInRegion() && giveup.isLocked() == false) {
            giveup.setActive(true);
            if (Gdx.input.isTouched()) {
                gameStateManager.pop();
            }
        } else {
            giveup.setActive(false);
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        spriteBatch.begin();
        for (int i = 0; i < 6; i++) {
            for (Pipe pipe : positions[i]) {
                if (!(pipe == null)) {
                    pipe.getDrawable().draw(spriteBatch);
                    if (pipe.correctRotations.length == 2) {
                        System.out.println(i + " " + pipe.rotation + " (" + pipe.correctRotations[0] + " " + pipe.correctRotations[1] + ") " + pipe.isCorrectRotation());
                    }
                    else {
                        System.out.println(i + " " + pipe.rotation + " (" + pipe.correctRotations[0] + ") " + pipe.isCorrectRotation());
                    }
                }
            }
        }
        spriteBatch.draw(giveup.getTexture(), giveup.getPosition().x, giveup.getPosition().y, giveup.getWidth(), giveup.getHeight());

        Boolean allCorrect = true;
        for (int i = 0; i < 6; i++) {
            for (Pipe pipe : positions[i]) {
                if(!(pipe == null)) {
                    if (Gdx.input.justTouched() && pipe.inPipeRange()) {
                        pipe.rotate();
                    }
                    if (!pipe.isCorrectRotation()) {
                        allCorrect = false;
                    }
                }
            }
        }
        if (finished) {
            //Makes the 'minigame complete' sprite appear
            spriteBatch.draw(minigameComplete, 600, 600, 503, 73);
        }
        spriteBatch.end();
        if (allCorrect && !finished) {
            finished = true;
            //Sleeps for one second
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    gameStateManager.pop();
                }
            }, 1);
            //Returns to the game
        }
    }

    /**
     * Finds a random path from the start to the end, creating a sequence of pipes as it does so
     * @param pos The current position to be looked at
     * @param finalPos The destination
     * @param lastPos The point before the current position to be looked at
     */
    public void findPath(Vector2 pos, Vector2 finalPos, Vector2 lastPos) {
        System.out.println(pos.toString());
        if (pos.y == finalPos.y && pos.x == 4 || test == 100) {
            //If we're the pipe away from the end then add the final pipe and end
            positions[(int) pos.x][(int) pos.y] = choosePipe(lastPos, pos, finalPos);
        }
        else {
            System.out.println(test);
            List<Vector2> directions = new ArrayList<Vector2> (Arrays.asList(new Vector2(0,1),
                    new Vector2(0,-1), new Vector2(1,0), new Vector2(-1,0)));
            //Check if left movement is valid
            if (pos.x - 1 < 1 || !(positions[(int) pos.x - 1][(int) pos.y] == null)
                    || ((pos.y + 1 > 3 || !(positions[(int) pos.x - 1][(int) pos.y + 1] == null))
                    && (pos.y - 1 < 0 || !(positions[(int) pos.x - 1][(int) pos.y - 1] == null)))) {
                directions.remove(new Vector2(-1,0));
            }
            //Check if right movement is valid
            if (pos.x + 1 > 4 || !(positions[(int) pos.x + 1][(int) pos.y] == null)
                    || ((pos.y + 1 > 3 || !(positions[(int) pos.x + 1][(int) pos.y + 1] == null))
                    && (pos.y - 1 < 0 || !(positions[(int) pos.x + 1][(int) pos.y - 1] == null))
                    && (pos.x + 2 > 4 || !(positions[(int) pos.x + 2][(int) pos.y] == null)))) {
                directions.remove(new Vector2(1,0));
            }
            //Check if up movement is valid
            if (pos.y + 1 > 3 || !(positions[(int) pos.x][(int) pos.y + 1] == null)
                    || ((pos.x + 1 > 4 || !(positions[(int) pos.x + 1][(int) pos.y + 1] == null))
                    && (pos.x - 1 < 1 || !(positions[(int) pos.x - 1][(int) pos.y + 1] == null))
                    && (pos.y + 2 > 3 || !(positions[(int) pos.x][(int) pos.y + 2] == null)))) {
                directions.remove(new Vector2(0,1));
            }
            //Check if down movement is valid
            if (pos.y - 1 < 0 || !(positions[(int) pos.x][(int) pos.y - 1] == null)
                    || ((pos.x + 1 > 4 || !(positions[(int) pos.x + 1][(int) pos.y - 1] == null))
                    && (pos.x - 1 < 1 || !(positions[(int) pos.x - 1][(int) pos.y - 1] == null))
                    && (pos.y - 2 < 0 || !(positions[(int) pos.x][(int) pos.y - 2] == null)))) {
                directions.remove(new Vector2(0,-1));
            }
            Vector2 nextPos = pos.cpy();
            if (directions.size() == 0) {
                test = 100;
                System.out.println("No further moves");
            }
            else {
                nextPos.add(directions.get((new Random()).nextInt(directions.size())));
                positions[(int) pos.x][(int) pos.y] = choosePipe(lastPos, pos, nextPos);
                System.out.println(positions);
                test++;
                findPath(nextPos, finalPos, pos);
            }
        }
    }

    public Pipe choosePipe(Vector2 lastPos, Vector2 currentPos, Vector2 nextPos) {
        //If last pipe was behind
        if (lastPos.x == currentPos.x - 1) {
            //Straight pipe across
            if (nextPos.x == currentPos.x + 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("straightPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {90, 270});
            }
            //Pipe bending forwards + upwards
            else if (nextPos.y == currentPos.y + 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {180});
            }

            //Pipe bending forwards + downwards
            else if (nextPos.y == currentPos.y - 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {270});
            }
        }
        //If last pipe was above
        else if (lastPos.y == currentPos.y + 1) {
            //Straight pipe down
            if (nextPos.y == currentPos.y - 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("StraightPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {0, 180});
            }
            //Pipe bending down and to the right
            else if (nextPos.x == currentPos.x + 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {90});
            }
            //Pipe bending down and to the left
            else if (nextPos.x == currentPos.x - 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {180});
                }
            }
        //If last pipe was below
        else if (lastPos.y == currentPos.y - 1) {
            //Straight pipe up
            if (nextPos.y == currentPos.y + 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("StraightPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {0, 180});
            }
            //Pipe bending up and to the right
            else if (nextPos.x == currentPos.x + 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {0});
            }
            //Pipe bending up and to the left
            else if (nextPos.x == currentPos.x - 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {270});
            }
        }
        //If last pipe was to the right
        else if (lastPos.x == currentPos.x + 1) {
            //Straight pipe across
            if (nextPos.x == currentPos.x- 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("StraightPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {90, 270});
            }
            //Pipe bending to the left and up
            else if (nextPos.y == currentPos.y + 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {90});
            }
            //Pipe bending to the left and down
            else if (nextPos.y == currentPos.y - 1) {
                return new Pipe(new Vector2(500 + 100 * currentPos.x, 400 + 100 *  currentPos.y), 100,
                        100, new Texture ("BendyPipe.png"),
                        rotations[(new Random()).nextInt(rotations.length)], new int[] {0});
            }
        }
        System.out.println("null returned");
        return null;
    }

    @Override
    public void dispose() {

    }
}
