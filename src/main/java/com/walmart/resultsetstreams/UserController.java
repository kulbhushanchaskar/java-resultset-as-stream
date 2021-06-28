package com.walmart.resultsetstreams;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@GetMapping("/getusers")
	public ResponseEntity<Stream<User>> getCurrentUser() {
		Stream<User> userSteream = fetchRecords();

		return ResponseEntity.ok().body(userSteream.takeWhile(u -> u != null));
	}

	public Stream<User> fetchRecords() {
		ResultSetStream<User> resStream = new ResultSetStream<>();
		Connection con = getConnection();

		Function<ResultSet, User> userFunction = res -> {
			try {
				return new User(res.getString("empid"), res.getString("empname"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return new User();
		};
		PreparedStatement st = null;
		try {
			st = con.prepareStatement("Select * from TBL_EMPLOYEES");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Stream<User> userSteream = Stream.empty();
		try {
			userSteream = resStream.getStream(st, userFunction);

			System.out.println("printing data");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Exiting");
		return userSteream;
	}

	public static Connection getConnection() {
		Connection con = null;
		try {
			Class.forName("org.h2.Driver");
			con = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return con;
		}
	}
