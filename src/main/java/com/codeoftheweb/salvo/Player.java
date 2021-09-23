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
    private String password;

    //OtM gamePlayer
    @OneToMany(mappedBy = "playerID", fetch = FetchType.EAGER)
    Set<GamePlayer>gamePlayers = new HashSet<>();

    //OtM score
    @OneToMany(mappedBy = "playerID", fetch = FetchType.EAGER)
    Set<Score> scores = new HashSet<>();

        public Player() {}

    public Player(String userName, String password) {
        this.userName = userName;
        this.password = password;
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


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
            gamePlayer.setPlayerID(this);;
            gamePlayers.add(gamePlayer);
        }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }

    @JsonIgnore
        public List<Game> getGames() {
            return gamePlayers.stream().map(sub -> sub.getGameID()).collect(toList());
        }

        //Metodo getScore
        //Filtro el game que es igual al "this.gameID" que le paso desde gameplayer y obtengo un score de mi lista de scores
       public Score getScore(Game game){
       return scores.stream().filter(sc->sc.getGameID().getId() == game.getId()).findFirst().orElse(null) ;
        }

}