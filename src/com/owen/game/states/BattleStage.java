package com.owen.game.states;

public enum BattleStage {
    PlayerSelection("Your Turn"),
    PlayerMove("Moving"),
    //    PlayerAttackChoice("Select Attacks"),
    PlayerAttack("Attacking"),
    EnemyMoveSelection ("Their Turn"),
    EnemyMove ("Their Turn"),
    EnemyAttackChoice("Their Turn"),
    EnemyAttack("Their Turn"),
    LossScreen("You Lose"),
    WinScreen("You Win"),
    ;

    private String name;

    BattleStage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
