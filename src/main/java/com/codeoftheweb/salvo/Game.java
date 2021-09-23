package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jayway.jsonpath.JsonPath;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import static java.util.stream.Collectors.toList;


@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private Date creationDate;

    //OtM gamePlayer
    @OneToMany(mappedBy= "gameID", fetch=FetchType.EAGER)
    Set<GamePlayer> gamePlayers = new HashSet<>();

    //OtM score
    @OneToMany(mappedBy = "gameID", fetch = FetchType.EAGER)
    Set<Score> score= new HashSet<>();


    public Game() {}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Game(Date creationDate) {
            this.creationDate = creationDate;
        }

        public Date getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(Date creationDate) {
            this.creationDate = creationDate;
        }

        public Game(Set<GamePlayer> gamePlayer) {
            this.gamePlayers = gamePlayer;
        }

        public Set<GamePlayer> getGamePlayer() {
            return gamePlayers;
        }

        public void setGamePlayer(Set<GamePlayer> gamePlayer) {
            this.gamePlayers = gamePlayer;
        }


       public void addGamePlayer(GamePlayer gamePlayer) {
            gamePlayer.setGameID(this);
            gamePlayers.add(gamePlayer);
        }

        //@JsonIgnore
        public List<Player> getPlayers() {
            return gamePlayers.stream().map(sub -> sub.getPlayerID()).collect(toList());
        }


    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Set<Score> getScore() {
        return score;
    }

    public void setScore(Set<Score> score) {
        this.score = score;
    }



}


