package com.codeoftheweb.salvo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
		return args -> {

			//playerRepository.save(new Player("player4@gmail.com"));
			//El dato se puede agregar directo pero es recomendable asignarlo a una variable.

			//Ej players
			Player player1 = new Player("j.bauer@ctu.gov");
			Player player2 = new Player("c.obrian@ctu.gov");
			Player player3 = new Player("t.almeida@ctu.gov");
			Player player4 = new Player("d.palmer@whitehouse.gov");
			playerRepository.save(player1);
			playerRepository.save(player2);
			playerRepository.save(player3);
			playerRepository.save(player4);

			//EJ games
			Date creationDate = new Date();
			Game game1= new Game(creationDate);
			Game game2= new Game(Date.from(creationDate.toInstant().plusSeconds(3600)));
			Game game3= new Game(Date.from(creationDate.toInstant().plusSeconds(7200)));
			Game game4= new Game(Date.from(creationDate.toInstant().plusSeconds(10800)));
			Game game5= new Game(Date.from(creationDate.toInstant().plusSeconds(14400)));
			Game game6= new Game(Date.from(creationDate.toInstant().plusSeconds(18000)));
			gameRepository.save(game1);
			gameRepository.save(game2);
			gameRepository.save(game3);
			gameRepository.save(game4);
			gameRepository.save(game5);
			gameRepository.save(game6);

			//EJ game player
			Date joinDate = new Date();
			GamePlayer gamePlayer1 = new GamePlayer(joinDate,player1,game1);
			GamePlayer gamePlayer2 = new GamePlayer(joinDate,player2,game1);
			GamePlayer gamePlayer3 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(3600)), player1, game2);
			GamePlayer gamePlayer4 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(3600)),player2,game2);
			GamePlayer gamePlayer5 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(7200)),player2,game3);
			GamePlayer gamePlayer6 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(7200)),player3,game3);
			GamePlayer gamePlayer7 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(10800)),player1,game4);
			GamePlayer gamePlayer8 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(10800)),player2,game4);
			GamePlayer gamePlayer9 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(14400)),player3,game5);
			GamePlayer gamePlayer10 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(14400)),player1,game5);
			GamePlayer gamePlayer11 = new GamePlayer(Date.from(joinDate.toInstant().plusSeconds(18000)),player4,game6);

			gamePlayerRepository.save(gamePlayer1);
			gamePlayerRepository.save(gamePlayer2);
			gamePlayerRepository.save(gamePlayer3);
			gamePlayerRepository.save(gamePlayer4);
			gamePlayerRepository.save(gamePlayer5);
			gamePlayerRepository.save(gamePlayer6);
			gamePlayerRepository.save(gamePlayer7);
			gamePlayerRepository.save(gamePlayer8);
			gamePlayerRepository.save(gamePlayer9);
			gamePlayerRepository.save(gamePlayer10);
			gamePlayerRepository.save(gamePlayer11);

		};
	}
}