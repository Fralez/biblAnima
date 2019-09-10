package logica;

import java.util.Date;

public class Prestamo {
	private int idGenerator = 0;
	
	private int id;
	private Date fechaSolicitado;
	private Date fechaDevolucion;
	private Usuario usuario;
	private int idLibro;
	
	public Prestamo(Date fechaSolicitado, Date fechaDevolucion, Usuario usuario, int idLibro) {
		this.idGenerator++;
		
		this.id = this.idGenerator;
		this.fechaSolicitado = fechaSolicitado;
		this.fechaDevolucion = fechaDevolucion;
		this.usuario = usuario;
		this.idLibro = idLibro;
		
		this.usuario.addPrestamo(this);
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getIdLibro() {
		return idLibro;
	}
	public void setIdLibro(int idLibro) {
		this.idLibro = idLibro;
	}

	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Date getFechaSolicitado() {
		return fechaSolicitado;
	}
	public void setFechaSolicitado(Date fechaSolicitado) {
		this.fechaSolicitado = fechaSolicitado;
	}

	public Date getFechaDevolucion() {
		return fechaDevolucion;
	}
	public void setFechaDevolucion(Date fechaDevolucion) {
		this.fechaDevolucion = fechaDevolucion;
	}
	
}
