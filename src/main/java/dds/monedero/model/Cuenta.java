package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  // iniciar el saldo en 0 no tiene sentido ya que hacemos lo mismo en el constructor
  private double saldo;// = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }


  /*public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }*/

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }


  public void poner(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
    // aca tenemos un caso de type test
    if (getMovimientos().stream().filter(movimiento -> movimiento.esDeposito).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
    // el code smell mas similar a lo que veo aca seria inappropriate intimacy ya que lo de agregar a la lista de movimientos lo podria hacer la cuenta directamenta
    //new Movimiento(LocalDate.now(), cuanto, true).agregateA(this);
    this.realizarOperacion(LocalDate.now(), cuanto, true);
  }

  public void sacar(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
    // es casi igual al del metodo poner
    //new Movimiento(LocalDate.now(), cuanto, false).agregateA(this);
    this.realizarOperacion(LocalDate.now(), cuanto, false);
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
  }

  // aca tenemos el tema del type test en isDeposito
  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.esDeposito && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public void realizarOperacion(LocalDate fecha, double monto, boolean esDeposito) {
    this.setSaldo(calcularValor(monto, esDeposito));
    this.agregarMovimiento(fecha, monto, esDeposito);
  }

  public double calcularValor(double monto, boolean esDeposito) {
    if (esDeposito) {
      return this.getSaldo() + monto;
    } else {
      return this.getSaldo() - monto;
    }
  }

  public boolean fueDepositado(LocalDate fecha) {
    return getMovimientos().stream().anyMatch(movimiento -> movimiento.esDeposito && movimiento.esDeLaFecha(fecha));
  }

  public boolean fueExtraido(LocalDate fecha) {
    return getMovimientos().stream().anyMatch(movimiento -> !movimiento.esDeposito && movimiento.esDeLaFecha(fecha));
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
