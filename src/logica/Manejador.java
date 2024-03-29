package logica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import persistencia.Conn;

public class Manejador {
	private Manejador() {
		// When the Manejador is initialised the data from DB is fetched
		this.fetchData();
	}

	private static final Manejador instance = new Manejador();

	private Conn connect = new Conn();
	private Connection con = connect.conectarMySQL();
	private Statement s;

	private ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
	private ArrayList<Libro> libros = new ArrayList<Libro>();

	public static Manejador getInstance() {
		return instance;
	}

	private void fetchData() {
		ResultSet rsUsuarios;
		ResultSet rsLibros;
		ResultSet rsPrestamos;
		Statement sPrestamos;

		try {
			s = con.createStatement();
			sPrestamos = con.createStatement();

			rsLibros = s.executeQuery("SELECT * FROM Libro");
			while (rsLibros.next()) {
				libros.add(new Libro(rsLibros.getString("aniCode"), rsLibros.getString("autor"),
						rsLibros.getInt("yearPubl"), rsLibros.getInt("nroedicion"), rsLibros.getString("editorial"),
						rsLibros.getString("descripcion"), rsLibros.getInt("cantEjemplares"),
						rsLibros.getInt("cantEjemplaresDisp"), rsLibros.getBoolean("hayEjemplarDisponible"),
						rsLibros.getString("codigoISBN"), rsLibros.getString("genero"), rsLibros.getString("imagURL"),
						rsLibros.getString("titulo")));
			}

			rsUsuarios = s.executeQuery("SELECT * FROM Usuario;");
			while (rsUsuarios.next()) {

				rsPrestamos = sPrestamos
						.executeQuery("SELECT * FROM Prestamo WHERE idUsuario = " + rsUsuarios.getInt("id") + ";");

				Usuario usuario = new Usuario(rsUsuarios.getInt("id"), rsUsuarios.getInt("ci"),
						rsUsuarios.getString("nombre"), rsUsuarios.getString("apellido"), rsUsuarios.getString("mail"),
						rsUsuarios.getString("password"));

				while (rsPrestamos.next()) {
					Libro libroPrestamo = consultaLibro(rsPrestamos.getString("aniCode"));
					new Prestamo(rsPrestamos.getInt("id"), rsPrestamos.getDate("fechaDevolucion"),
							rsPrestamos.getDate("fechaSolicitado"), rsPrestamos.getBoolean("devuelto"), usuario,
							libroPrestamo);
				}
				usuarios.add(usuario);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Boolean login(int ci, String password) {
		for (int i = 0; i < usuarios.size(); i++) {
			if (usuarios.get(i).getCi() == ci && usuarios.get(i).getPassword().equals(password)) {
				return true;
			}
		}
		return false;
	}

	public int generarID(String obj) {
		ResultSet rs;
		int id = 0;
		switch (obj) {
		case "usuario":
			try {
				s = con.createStatement();
				rs = s.executeQuery("SELECT MAX(u.id) as \"id\" FROM Usuario u");
				if (rs.next()) {
					id = rs.getInt("id");
				} else {
					id = 0;
				}
				return id + 1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;

		case "prestamo":
			try {
				s = con.createStatement();
				rs = s.executeQuery("SELECT MAX(p.id) as \"id\" FROM Prestamo p");
				if (rs.next()) {
					id = rs.getInt("id");
				} else {
					id = 0;
				}
				return id + 1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		}
		return 0;
	}

	public void altaUsuario(int ci, String nombre, String apellido, String mail, String password, TipoUsuario tipo,
			Orientacion orient) throws SQLException {

		Usuario usuario = null;
		// Query para insertar un usuario en una de las tablas de usuario espec�fico
		// (Estudiante, Profesor o Bibliotecario)
		String typeQuery = "";
		// Id de usuario
		int id = generarID("usuario");

		switch (tipo) {
		case ESTUDIANTE:
			usuario = new Estudiante(id, ci, nombre, apellido, mail, password, orient);
			typeQuery = "INSERT INTO Estudiante(id, orient, tope) VALUES(" + usuario.getId() + ", '" + orient + "', "
					+ (((Estudiante) usuario).getTope()) + ");";
			break;
		case PROFESOR:
			usuario = new Profesor(id, ci, nombre, apellido, mail, password, orient);
			typeQuery = "INSERT INTO Profesor(id, orient) VALUES(" + usuario.getId() + ", '" + orient + "');";
			break;
		case BIBLIOTECARIO:
			usuario = new Bibliotecario(id, ci, nombre, apellido, mail, password);
			typeQuery = "INSERT INTO Bibliotecario(id) VALUES(" + usuario.getId() + ");";
			break;
		}

		try {
			s = con.createStatement();
			s.executeUpdate("INSERT INTO Usuario(id, ci, nombre, apellido, mail, password) VALUES(" + usuario.getId()
					+ ", " + ci + ", '" + nombre + "', '" + apellido + "', '" + mail + "', '" + password + "');");
			s.executeUpdate(typeQuery);
			// A�ade al usuario en el array
			usuarios.add(usuario);
		} catch (SQLException e) {
			throw e;
		}

	}

	public Usuario consultaUsuario(int ci) {
		for (int i = 0; i < usuarios.size(); i++) {
			if (usuarios.get(i).getCi() == ci) {
				return usuarios.get(i);
			}
		}

		return null;
	}

	public void modificarDatosUsuario(int ci, String nombre, String apellido, String mail, String password)
			throws SQLException {
		try {
			s = con.createStatement();
			s.executeUpdate("UPDATE Usuario SET nombre = '" + nombre + "', apellido = '" + apellido + "', mail = '"
					+ mail + "', password = '" + password + "' WHERE Usuario.ci = " + ci + ";");
			// Actualizar el usuario en el array
			for (int i = 0; i < usuarios.size(); i++) {
				if (usuarios.get(i).getCi() == ci) {
					usuarios.get(i).setNombre(nombre);
					usuarios.get(i).setApellido(apellido);
					usuarios.get(i).setMail(mail);
					usuarios.get(i).setPassword(password);
				}
			}
		} catch (SQLException e) {
			throw e;
		}

	}

	public ArrayList<Usuario> listarUsuariosExistentes() {
		return usuarios;
	}

	public ArrayList<Libro> listarLibrosExistentes() {
		return libros;
	}

	public Libro consultaLibro(String aniCode) {
		for (int i = 0; i < libros.size(); i++) {
			if (libros.get(i).getAniCode().equals(aniCode)) {
				return libros.get(i);
			}
		}
		return null;
	}

	public Prestamo consultaPrestamo(int id) {
		for (int i = 0; i < listarPrestamos().size(); i++) {
			if (listarPrestamos().get(i).getId() == id) {
				return listarPrestamos().get(i);
			}
		}
		return null;
	}

	public ArrayList<Prestamo> listarPrestamos() {
		ArrayList<Prestamo> prestamos = new ArrayList<Prestamo>();

		for (int i = 0; i < usuarios.size(); i++) {
			prestamos.addAll(usuarios.get(i).getPrestamos());
		}

		return prestamos;
	}

	public void altaPrestamo(String sFechaDev, Usuario usuario, Libro libro) throws Exception {
		// Termina la funcion si el usuario es Estudiante y tiene ya 2 prestamos activos
//		if (((Estudiante) usuario).getTope() > -1) {
//			throw new Exception("Tope alcanzado");
//		}

		if (libro.hayEjemplarDisponible()) {
			// Id de prestamo
			int id = generarID("prestamo");

			try {
				s = con.createStatement();
				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				Date fechaDev = formatter.parse(sFechaDev);
				new Prestamo(id, fechaDev, new Date(), false, usuario, libro);

				s.executeUpdate(
						"INSERT INTO Prestamo (id, fechaSolicitado, fechaDevolucion, devuelto, idUsuario, aniCode) VALUES("
								+ id + ", CURTIME()" + ", STR_TO_DATE('" + sFechaDev + "', '%m/%d/%Y')" + ", " + 0
								+ ", " + usuario.getId() + ", '" + libro.getAniCode() + "');");

				// Incrementar el tope en uno si el usuario es Estudiante
//				if (((Estudiante) usuario).getTope() > -1) {
//					// En el array
//					((Estudiante) usuario).setTope(((Estudiante) usuario).getTope() + 1);
//					// En la base de datos
//					s.executeUpdate("UPDATE Usuario u SET u.tope = " + ((Estudiante) usuario).getTope()
//							+ " WHERE u.id = " + usuario.getId() + ";");
//				}

				// Suplanto el usuario viejo poro el actualizado en el array
				for (int i = 0; i < usuarios.size(); i++) {
					if (usuarios.get(i).getCi() == usuario.getCi()) {
						usuarios.remove(i);
						usuarios.add(usuario);
					}
				}

				if (libro.getCantEjemplaresDisp() > 0) {
					libro.setCantEjemplaresDisp(libro.getCantEjemplaresDisp() - 1);

					// Modificar la base de datos con el nuevo cant de ejemplares disponibles
					s.executeUpdate("UPDATE Libro SET cantEjemplaresDisp = " + libro.getCantEjemplaresDisp()
							+ " WHERE aniCode = '" + libro.getAniCode() + "';");

					if (libro.getCantEjemplaresDisp() < 1) {
						// Modificar en el array con el nuevo hayEjemplarDisponible
						libro.setHayEjemplarDisponible(false);
						// Modificar la base de datos con el nuevo hayEjemplarDisponible
						s.executeUpdate("UPDATE Libro SET hayEjemplarDisponible = " + libro.hayEjemplarDisponible()
								+ " WHERE aniCode = '" + libro.getAniCode() + "';");
					}

					// Cambiar el libro actualizado por su equivalente en el array de libros
					for (int i = 0; i < libros.size(); i++) {
						if (libros.get(i).getAniCode() == libro.getAniCode()) {
							libros.remove(i);
							libros.add(libro);
						}
					}
				}
			} catch (SQLException e) {
				throw e;
			}
		} else {
			throw new Exception("No hay ejemplares disponibles");
		}
	}

	public void altaLibro(String aniCode, String Autor, int yearPubl, int nroEdicion, String editorial,
			String descripcion, int cantEjemplares, boolean hayEjemplarDisponible, String codigoISBN, String genero,
			String ImagURL, String titulo) throws SQLException {

		Libro libro = new Libro(aniCode, Autor, yearPubl, nroEdicion, editorial, descripcion, cantEjemplares,
				cantEjemplares, hayEjemplarDisponible, codigoISBN, genero, ImagURL, titulo);

		try {
			s.executeUpdate(
					"INSERT INTO Libro(aniCode, autor, yearPubl, nroEdicion, editorial, descripcion, cantEjemplares, cantEjemplaresDisp, hayEjemplarDisponible, codigoISBN, genero, imagUrl, titulo) VALUES('"
							+ aniCode + "', '" + Autor + "', " + yearPubl + ", " + nroEdicion + ", '" + editorial
							+ "', '" + descripcion + "', " + cantEjemplares + ", " + cantEjemplares + ", "
							+ hayEjemplarDisponible + ", '" + codigoISBN + "', '" + genero + "', '" + ImagURL + "', '"
							+ titulo + "');");
			libros.add(libro);
		} catch (SQLException e) {
			throw e;
		}

	}

	public void darDeBajaUnPrestamo(Prestamo prestamo) {
		try {
			s = con.createStatement();
			// Recorrer el array de usuarios para tener acceso a todos los datos del array
			for (int i = 0; i < usuarios.size(); i++) {

				// Reconocer el usuario que hizo el prestamo
				if (prestamo.getUsuario().getCi() == usuarios.get(i).getCi()) {
					// Decrementar el tope en uno si el usuario es Estudiante
//					if (usuarios.get(i) instanceof Estudiante) {
//						// En la base de datos
//						s.executeUpdate("UPDATE Usuario SET tope = " + (((Estudiante) usuarios.get(i)).getTope() - 1)
//								+ " WHERE Usuario.id = " + usuarios.get(i).getId() + ";");
//						// En el array
//						((Estudiante) usuarios.get(i)).setTope(((Estudiante) usuarios.get(i)).getTope() - 1);
//					}

					// Recorrer el array de prestamos del usuario reconocido
					for (int j = 0; j < usuarios.get(i).getPrestamos().size(); j++) {
						// Reconocer el prestamo a dar de baja
						if (prestamo.getId() == usuarios.get(i).getPrestamos().get(j).getId()) {
							// Actualizo la devolucion del prestamo
							usuarios.get(i).getPrestamos().get(j).setDevuelto(true);
							// Luego lo actualizo en la base de datos
							s.executeUpdate("UPDATE Prestamo SET devuelto = 1 WHERE Prestamo.id = " + prestamo.getId());

							// Actualizo la cantidad de ejemplares disponibles del libro prestado en +1
							usuarios.get(i).getPrestamos().get(j).getLibro().setCantEjemplaresDisp(
									usuarios.get(i).getPrestamos().get(j).getLibro().getCantEjemplaresDisp() + 1);
							// Luego lo actualizo en la base de datos
							s.executeUpdate("UPDATE Libro SET cantEjemplaresDisp = "
									+ usuarios.get(i).getPrestamos().get(j).getLibro().getCantEjemplaresDisp()
									+ ", hayEjemplarDisponible = 1 WHERE Libro.aniCode = '"
									+ usuarios.get(i).getPrestamos().get(j).getLibro().getAniCode() + "';");
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
