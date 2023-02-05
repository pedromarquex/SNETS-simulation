package grmlsa.csa;

import java.util.List;
import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

public class Pgnie implements CoreAndSpectrumAssignmentAlgorithmInterface {
  private int cont = 1;
  
  private int tentativa;
  
  private int limite1 = 107;
  
  private int limite2 = 204;
  
  public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
    int chosenCore = coreAssignment(numberOfSlots, circuit);
    int[] chosen = null;
    List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), 
        chosenCore);
    this.tentativa = 1;
    for (int i = 0; i < 2; ) {
      if (chosenCore == 0) {
        chosen = policy(numberOfSlots, composition, circuit, cp);
      } else if (chosenCore % 2 == 0) {
        chosen = policy1(numberOfSlots, composition, circuit, cp);
      } else if (chosenCore % 2 == 1) {
        chosen = policy2(numberOfSlots, composition, circuit, cp);
      } 
      if (chosen == null) {
        this.tentativa = 2;
        chosenCore = coreAssignment(numberOfSlots, circuit);
        composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), chosenCore);
        i++;
      } 
      break;
    } 
    circuit.setSpectrumAssigned(chosen);
    circuit.setIndexCore(chosenCore);
    if (chosen == null)
      return false; 
    return true;
  }
  
  public int coreAssignment(int numberOfSlots, Circuit circuit) {
    int[] cores = new int[7];
    if (this.cont == 4)
      this.cont = 1; 
    switch (this.cont) {
      case 1:
        cores[0] = 1;
        cores[1] = 3;
        cores[2] = 5;
        cores[3] = 4;
        cores[4] = 6;
        cores[5] = 2;
        cores[6] = 0;
        break;
      case 2:
        cores[0] = 3;
        cores[1] = 5;
        cores[2] = 1;
        cores[3] = 6;
        cores[4] = 2;
        cores[5] = 4;
        cores[6] = 0;
        break;
      case 3:
        cores[0] = 5;
        cores[1] = 1;
        cores[2] = 3;
        cores[3] = 2;
        cores[4] = 4;
        cores[5] = 6;
        cores[6] = 0;
        break;
    } 
    this
      
      .cont = this.cont + 1;
    int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
    if (numberOfSlots > maxAmplitude)
      return 0; 
    int[] chosen = null;
    byte b;
    int i, arrayOfInt1[];
    for (i = (arrayOfInt1 = cores).length, b = 0; b < i; ) {
      int core = arrayOfInt1[b];
      List<int[]> freeSpectrumBands = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(), 
          core);
      for (int[] band : freeSpectrumBands) {
        if (chosen == null)
          switch (core) {
            case 1:
            case 3:
            case 5:
              if (this.tentativa == 1) {
                if (band[0] + numberOfSlots - 1 < this.limite1 && band[1] - band[0] + 1 >= numberOfSlots)
                  return core; 
                continue;
              } 
              if (band[0] >= this.limite1 && band[1] - band[0] + 1 >= numberOfSlots)
                return core; 
            case 2:
            case 4:
            case 6:
              if (this.tentativa == 1) {
                if (band[0] > this.limite1 && band[0] + numberOfSlots - 1 < this.limite2 && 
                  band[1] - band[0] + 1 >= numberOfSlots)
                  return core; 
                if ((band[0] + numberOfSlots - 1 < this.limite1 || band[0] >= this.limite2) && 
                  band[1] - band[0] + 1 >= numberOfSlots)
                  return core; 
              } 
            case 0:
              if (this.tentativa == 1) {
                if (band[0] >= this.limite2 && band[1] - band[0] + 1 >= numberOfSlots)
                  return core; 
                continue;
              } 
              if (band[0] + numberOfSlots - 1 < this.limite2 && band[1] - band[0] + 1 >= numberOfSlots)
                return core; 
          }  
      } 
      b++;
    } 
    return 0;
  }
  
  public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
    int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
    if (numberOfSlots > maxAmplitude)
      return null; 
    int[] chosen = null;
    if (this.tentativa == 1) {
      for (int[] band : freeSpectrumBands) {
        if (band[0] >= this.limite2 && 
          band[1] - band[0] + 1 >= numberOfSlots) {
          chosen = (int[])band.clone();
          chosen[1] = chosen[0] + numberOfSlots - 1;
          break;
        } 
      } 
    } else {
      int[] band = null;
      for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
        band = freeSpectrumBands.get(i);
        if (band[0] + numberOfSlots - 1 < this.limite2 && 
          band[1] - band[0] + 1 >= numberOfSlots) {
          chosen = (int[])band.clone();
          chosen[0] = chosen[1] - numberOfSlots + 1;
          break;
        } 
      } 
    } 
    return chosen;
  }
  
  public int[] policy1(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
    int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
    if (numberOfSlots > maxAmplitude)
      return null; 
    int[] chosen = null;
    if (this.tentativa == 1) {
      for (int[] band : freeSpectrumBands) {
        if (band[0] > this.limite1 && band[0] + numberOfSlots - 1 < this.limite2 && 
          band[1] - band[0] + 1 >= numberOfSlots) {
          chosen = (int[])band.clone();
          chosen[1] = chosen[0] + numberOfSlots - 1;
          break;
        } 
      } 
    } else {
      int[] band = null;
      for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
        band = freeSpectrumBands.get(i);
        if ((band[0] + numberOfSlots - 1 < this.limite1 || band[0] >= this.limite2) && 
          band[1] - band[0] + 1 >= numberOfSlots) {
          chosen = (int[])band.clone();
          chosen[0] = chosen[1] - numberOfSlots + 1;
          break;
        } 
      } 
    } 
    return chosen;
  }
  
  public int[] policy2(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
    int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
    if (numberOfSlots > maxAmplitude)
      return null; 
    int[] chosen = null;
    if (this.tentativa == 1) {
      for (int[] band : freeSpectrumBands) {
        if (band[0] + numberOfSlots - 1 < this.limite1 && 
          band[1] - band[0] + 1 >= numberOfSlots) {
          chosen = (int[])band.clone();
          chosen[1] = chosen[0] + numberOfSlots - 1;
          break;
        } 
      } 
    } else {
      int[] band = null;
      for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
        band = freeSpectrumBands.get(i);
        if (band[0] >= this.limite1 && 
          band[1] - band[0] + 1 >= numberOfSlots) {
          chosen = (int[])band.clone();
          chosen[0] = chosen[1] - numberOfSlots + 1;
          break;
        } 
      } 
    } 
    return chosen;
  }
  
  public int coreAssignment() {
    return 0;
  }
}

