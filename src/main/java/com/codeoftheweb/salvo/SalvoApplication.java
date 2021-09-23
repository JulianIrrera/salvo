package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

	@Autowired PasswordEncoder passwordEncoder;


	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
		return args -> {

			//playerRepository.save(new Player("player4@gmail.com"));
			//El dato se puede agregar directo pero es recomendable asignarlo a una variable.

			//Ej players
			Player player1 = new Player("j.bauer@ctu.gov",passwordEncoder.encode("24"));
			Player player2 = new Player("c.obrian@ctu.gov",passwordEncoder.encode("42"));
			Player player3 = new Player("t.almeida@ctu.gov",passwordEncoder.encode("nole"));
			Player player4 = new Player("d.palmer@whitehouse.gov",passwordEncoder.encode("dp"));
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

			//EJ Ships
			Ship ship1 = new Ship("submarine", gamePlayer1, Arrays.asList("A2", "A3", "A4"));
			Ship ship2 = new Ship("battleship", gamePlayer1, Arrays.asList("F3","G3","H3","I3"));
			Ship ship3 = new Ship("submarine", gamePlayer2, Arrays.asList("C2","C3","C4"));
			Ship ship4 = new Ship("battleship", gamePlayer2, Arrays.asList("D6","E6","F6","G6"));
			Ship ship5 = new Ship("submarine", gamePlayer3, Arrays.asList("A2", "A3", "A4"));
			Ship ship6 = new Ship("battleship", gamePlayer3, Arrays.asList("G3","H3","I3","F3"));
			Ship ship7 = new Ship("submarine", gamePlayer4, Arrays.asList("B8","B9","B10"));
			Ship ship8 = new Ship("battleship", gamePlayer4, Arrays.asList("D4","E4","F4","G4"));
			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);
			shipRepository.save(ship6);
			shipRepository.save(ship7);
			shipRepository.save(ship8);

			//EJ Salvos
			Salvo salvo1 = new Salvo(gamePlayer1,1,Arrays.asList("D6", "G1"));
			Salvo salvo2 = new Salvo(gamePlayer1,2,Arrays.asList("E6", "F6"));
			Salvo salvo3 = new Salvo(gamePlayer2,1,Arrays.asList("I4", "A8"));
			Salvo salvo4 = new Salvo(gamePlayer2,2,Arrays.asList("A3", "H4"));
			Salvo salvo5 = new Salvo(gamePlayer3,1,Arrays.asList("B8", "C4"));
			Salvo salvo6 = new Salvo(gamePlayer3,2,Arrays.asList("E5", "F4"));
			Salvo salvo7 = new Salvo(gamePlayer4,1,Arrays.asList("A4", "B8"));
			Salvo salvo8 = new Salvo(gamePlayer4,2,Arrays.asList("G1", "I3"));
			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);
			salvoRepository.save(salvo3);
			salvoRepository.save(salvo4);
			salvoRepository.save(salvo5);
			salvoRepository.save(salvo6);
			salvoRepository.save(salvo7);
			salvoRepository.save(salvo8);

			//EJ Scores
			Date finishDate = new Date();
			Score score1 = new Score(1,Date.from(finishDate.toInstant().plusSeconds(1800)),game1,player1);
			Score score2 = new Score(0,Date.from(finishDate.toInstant().plusSeconds(1800)),game1,player2);
			Score score3 = new Score(0.5,Date.from(finishDate.toInstant().plusSeconds(5400)),game2,player1);
			Score score4 = new Score(0.5,Date.from(finishDate.toInstant().plusSeconds(5400)),game2,player2);
			Score score5 = new Score(1,Date.from(finishDate.toInstant().plusSeconds(9000)),game3,player2);
			Score score6 = new Score(0,Date.from(finishDate.toInstant().plusSeconds(9000)),game3,player3);
			Score score7 = new Score(0.5,Date.from(finishDate.toInstant().plusSeconds(12600)),game4,player1);
			Score score8 = new Score(0.5,Date.from(finishDate.toInstant().plusSeconds(12600)),game4,player2);
			scoreRepository.save(score1);
			scoreRepository.save(score2);
			scoreRepository.save(score3);
			scoreRepository.save(score4);
			scoreRepository.save(score5);
			scoreRepository.save(score6);
			scoreRepository.save(score7);
			scoreRepository.save(score8);

		};
	}
}


//Autenticacion
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}


	//@Autowired PasswordEncoder passwordEncoder;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(username-> {
			Player player = playerRepository.findByUserName(username);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown player: " + username);
			}
		});
	}
}


//Autorizacion
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired PasswordEncoder passwordEncoder;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/api/game_view/").hasAuthority("USER")
				.antMatchers("/api/games", "/api/players", "/api/login", "/web/**","api/games.html").permitAll()
				.antMatchers("/h2-console/").permitAll()
				.and().headers().frameOptions().disable()
				.and().csrf().ignoringAntMatchers("/h2-console/")
				.and()
				.cors().disable();
		http.authorizeRequests();



		http.formLogin()
				.usernameParameter("name")
				.passwordParameter("pwd")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");


		http.csrf().disable();

		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());

	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		}
	}



}


