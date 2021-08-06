package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private Date joinDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player playerID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private  Game gameID;

        public GamePlayer() {}


    public GamePlayer(Date joinDate, Player playerID, Game gameID) {
        this.joinDate = joinDate;
        this.playerID = playerID;
        this.gameID = gameID;
    }

    public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Date getJoinDate() {
            return joinDate;
        }

        public void setJoinDate(Date joinDate) {
            this.joinDate = joinDate;
        }


        public Player getPlayerID() {
            return playerID;
        }

        public void setPlayerID(Player playerID) {
            this.playerID = playerID;
        }

        public Game getGameID() {
            return gameID;
        }

        public void setGameID(Game gameID) {
            this.gameID = gameID;
        }
}
