package com.example.demo;



import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	// パスワードエンコーダーのBean定義
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


	@Autowired
	private DataSource dataSource;

	// ユーザIDとパスワードを取得するSQL
	private static final String USER_SQL = "SELECT user_id, password, true FROM m_user WHERE user_id = ?";

	// ユーザのロールを取得するSQL
	private static final String ROLE_SQL = "SELECT user_id, role FROM m_user WHERE user_id = ?";

	@Override
	public void configure(WebSecurity web) throws Exception{

		// 静的リソースへのアクセスにはセキュリティを適用しない
		web.ignoring().antMatchers("/webjars/**", "/css/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception{

		// ログイン不要ページの設定
		// webjars、css、ログイン画面、ユーザ登録画面への直リンクは可能
		// それ以外は不可
		http.authorizeRequests()
				.antMatchers("/webjars/**").permitAll()
				.antMatchers("/css/**").permitAll()
				.antMatchers("/login").permitAll()
				.antMatchers("/signup").permitAll()
				.antMatchers("/rest/**").permitAll()
				.antMatchers("/admin").hasAuthority("ROLE_ADMIN")
				.anyRequest().authenticated();

		// ログイン処理
		http.formLogin().loginProcessingUrl("/login")  // ログイン処理のパス
						.loginPage("/login")  // ログインページの指定
						.failureUrl("/login") // ログイン失敗時の遷移先
						.usernameParameter("userId") // ログインページのユーザID
						.passwordParameter("password") // ログインページのパスワード
						.defaultSuccessUrl("/home", true);  // ログイン成功後の遷移先

		// ログアウト処理
		http.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.logoutUrl("/logout")
					.logoutSuccessUrl("/login");



		// CSRF 対策を無効に設定（一時的）
		//http.csrf().disable();

		// CSRFを無効にするurlを設定
		RequestMatcher csrfMatcher = new RestMatcher("/rest/**");

		// RESTのみCSRFを無効に設定
		http.csrf().requireCsrfProtectionMatcher(csrfMatcher);

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {

		// ログイン処理時のユーザ情報をDBから取得
		auth.jdbcAuthentication()
						.dataSource(dataSource)
						.usersByUsernameQuery(USER_SQL)
						.authoritiesByUsernameQuery(ROLE_SQL)
						.passwordEncoder(passwordEncoder());
	}

}
