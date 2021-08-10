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
public class Player{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;
    private String userName;


    @OneToMany(mappedBy = "playerID", fetch = FetchType.EAGER)
    Set<GamePlayer>gamePlayers = new HashSet<>();

        public Player() {}

        public Player(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String firstName) {
            this.userName = firstName;
        }

        public Player(java.util.Set<GamePlayer> gamePlayer) {
            this.gamePlayers = gamePlayer;
        }

        public java.util.Set<GamePlayer> getGamePlayer() {
            return gamePlayers;
        }

        public void setPlayer(java.util.Set<GamePlayer> gamePlayer) {
            this.gamePlayers = gamePlayer;
        }


       public void addGamePlayer(GamePlayer gamePlayer) {
            gamePlayer.setPlayerID(this);;
            gamePlayers.add(gamePlayer);
        }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    @JsonIgnore
        public List<Game> getGames() {
            return gamePlayers.stream().map(sub -> sub.getGameID()).collect(toList());
        }

}