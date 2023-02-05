package grmlsa.csa;

import java.util.List;

import network.Circuit;
import network.ControlPlane;
import util.IntersectionFreeSpectrum;

public class Adein implements CoreAndSpectrumAssignmentAlgorithmInterface {

	private static final int COREQUANTITY = 7; // Quantidade de Ncleos na Fibra
	private static final int CENTRALCOREID = 0; // ID do ncleo Central
	private static final int DEFAULTPOINTFORCENTRALCORE = 15; // PONTUA��O INICIAL PARA N�CLEO CENTRAL
	private static final int DEFAULTPOINTFOROTHERCORE = 10; // PONTUA��O INICIAL PARA N�CLEOS DE BORDA
	private int[] pointArray = new int[COREQUANTITY]; // Tabela de Pontuao dos Ncleos
	private int[] coreUse = new int[COREQUANTITY]; // Quantas vezes cada nleo foi usado.
	// Inicializando tabela de pontos e definindo marcas de espectro.

	public Adein() {
		for (int i = 0; i < COREQUANTITY; i++) {
			pointArray[i] = DEFAULTPOINTFOROTHERCORE;
			coreUse[i] = 0;
		}
		pointArray[0] = DEFAULTPOINTFORCENTRALCORE;
	}

	@Override
	public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
		// Definindo Ncleo
		int chosenCore = coreAssignment();
		circuit.setIndexCore(chosenCore);
		// Declarando Varivel de Espectro Escolhido.
		int specChosen[] = null;
		// Buscando Composi Espectral do Ncleo Escolhido.
		List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute(), circuit.getGuardBand(),
				chosenCore);
		// Realizando Tentativa de Aloca com Primeira Poltica.
		specChosen = policy(numberOfSlots, composition, circuit, cp);
		// Realizando Tentativa de Alocacom Segunda Poltica.
		if (specChosen == null)
			specChosen = policy2(numberOfSlots, composition, circuit, cp);

		circuit.setSpectrumAssigned(specChosen);

		if (specChosen == null)
			return false;
		else
			return true;
	}

	@Override
	public int coreAssignment() {
		int chosenCore = checkPointTable(); // Escolhendo Ncleo Baseado na Tabela de Pontos.
		pointTableUpdate(chosenCore); // Atualizando Tabela de Pontos.
		return chosenCore;
	}

	private void pointTableUpdate(int currentCore) {
		// Ncleo Central.
		if (currentCore == CENTRALCOREID) {
			pointArray[CENTRALCOREID] = DEFAULTPOINTFORCENTRALCORE;
			for (int i = 1; i < COREQUANTITY; i++) {
				pointArray[i]--;
			}
			// Outros Ncleos.
		} else {
			int oppositeCore;
			int adjaLeftCore;
			int adjaRightCore;
			int adOposLeftCore;
			int adOposRightCore;
			// Definindo Ncleo Oposto
			if (currentCore < 4)
				oppositeCore = currentCore + 3;
			else
				oppositeCore = currentCore - 3;
			// Definindo Ncleo Adjacente Esquerdo
			if (currentCore == 1)
				adjaLeftCore = 6;
			else
				adjaLeftCore = currentCore - 1;
			// Definindo Ncleo Adjacente Direito
			if (currentCore == 6)
				adjaRightCore = 1;
			else
				adjaRightCore = currentCore + 1;
			// Definindo Ncleo Adjacente Oposto Esquerdo
			if (oppositeCore == 1)
				adOposLeftCore = 6;
			else
				adOposLeftCore = oppositeCore - 1;
			// Definindo Ncleo Adjacente Oposto Direito
			if (oppositeCore == 6)
				adOposRightCore = 1;
			else
				adOposRightCore = oppositeCore + 1;

			// Verificando Ncleos para Reset
			for (int i = 1; i < COREQUANTITY; i++) {
				if (pointArray[i] <= 0)
					pointArray[i] = DEFAULTPOINTFOROTHERCORE;
			}

			// Aplicando Tabela de Pontos
			pointArray[currentCore] = pointArray[currentCore] + 1; // Ncleo Atual
			pointArray[CENTRALCOREID] = pointArray[CENTRALCOREID] - 1; // Ncleo Central
			pointArray[oppositeCore] = pointArray[oppositeCore] - 1; // Nleo Oposto
			pointArray[adjaLeftCore] = pointArray[adjaLeftCore] - 2; // Ncleo Adjacente Esquerdo
			pointArray[adjaRightCore] = pointArray[adjaRightCore] - 2; // Ncleo Adjacente Direito
			pointArray[adOposLeftCore] = pointArray[adOposLeftCore] - 3; // Ncleo Adjacente Oposto Esquerdo
			pointArray[adOposRightCore] = pointArray[adOposRightCore] - 3; // Ncleo Adjacente Oposto Direito
		}
	}

	// Retorna o ID do Ncleo Escolhido.
	private int checkPointTable() {
		int menorValor = 999999;
		int coreID = -1;
		for (int i = 0; i < COREQUANTITY; i++) {
			if (pointArray[i] <= menorValor) {
				menorValor = pointArray[i];
				coreID = i;
			}
		}
		coreUse[coreID]++;
		return coreID;
	}

	// Primeira tentativa de aloca
	@Override
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		// Verificando se requisi��o est� dentro dos limites de amplitude do sinal
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
		if (numberOfSlots > maxAmplitude)
			return null;

		// Setando Divisores de Espectro
		int maxSpectrumSize = circuit.getRoute().getLink(0).getCore(0).getNumOfSlots();
		int minSpectrumRange = maxSpectrumSize / COREQUANTITY;
		int t1 = minSpectrumRange * 2;
		int t2 = minSpectrumRange * 4;
		int t3 = minSpectrumRange * 6;
		// System.out.println("Spec Size [" + maxSpectrumSize + "] T1 [" + t1 + "] T2 ["
		// + t2 + "] T3 [" + t3 + "]");
		// System.out.println("[" + circuit.getIndexCore() + "]");
//		System.out.println(
//				"CENTRAL [" + coreUse[CENTRALCOREID] + "] 1 - [" + coreUse[1] + "] 2 - [" + coreUse[2] + "] 3 - ["
//						+ coreUse[3] + "] 4 - [" + coreUse[4] + "] 5 - [" + coreUse[5] + "] 6 - [" + coreUse[6] + "]");

		// Escolhendo Espectro
		int chosen[] = null;

		// Core 1 - 4 (BLUE) First Fit
		if (circuit.getIndexCore() == 1 || circuit.getIndexCore() == 4) {
			for (int[] band : freeSpectrumBands) {
				if (band[1] - band[0] + 1 >= numberOfSlots && (band[0] + numberOfSlots - 1) < t1) {
					chosen = band.clone();
					chosen[1] = chosen[0] + numberOfSlots - 1;
//					System.out.println("1 - CORE [" + circuit.getIndexCore() + "] BLUE FIRST FIT REQUISIO ["
//							+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//							+ band[0] + " - " + band[1] + "]");
					return chosen;
				}
			}
		}
		// Core 2 - 5 (ORANGE) Medium Fit
		else if (circuit.getIndexCore() == 2 || circuit.getIndexCore() == 5) {
			int reference = t1 + (t1 / 2);
			int[] chosenBandLeft = null;
			int[] chosenBandRight = null;
			// Percorrendo Livres
			for (int[] band : freeSpectrumBands) {
				// Verificando se range aceita requisi��o
				if (band[1] - band[0] + 1 >= numberOfSlots) {
					// Cen�rio 1 - Esquerda - [0] [1] <= Reference
					if (band[0] < reference && band[1] <= reference && band[1] - numberOfSlots + 1 >= t1) {
						if (chosenBandLeft == null) {
							chosenBandLeft = band.clone();
							chosenBandLeft[0] = band[1] - numberOfSlots + 1;
						} else {
							if (reference - band[1] <= reference - chosenBandLeft[1]) {
								chosenBandLeft[1] = band[1];
								chosenBandLeft[0] = band[1] - numberOfSlots + 1;
							}
						}
					}
					// Cen�rio 2 - Dentro - [0] < Reference < [1]
					else if (band[0] <= reference && band[1] >= reference) {
						if ((reference - (numberOfSlots / 2)) > band[0]
								&& (reference + (numberOfSlots / 2)) < band[1]) {
							chosen = new int[2];
							chosen[0] = reference - (numberOfSlots / 2);
							chosen[1] = chosen[0] + numberOfSlots - 1;
//							System.out.println(
//									"1 - CORE [" + circuit.getIndexCore() + "] ORANGE IN MIDDLE FIT REQUISIO ["
//											+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1]
//											+ "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
							return chosen;
						} else {
							chosen = band.clone();
							chosen[1] = chosen[0] + numberOfSlots - 1;
//							System.out.println(
//									"1 - CORE [" + circuit.getIndexCore() + "] ORANGE ELSE IN FIRST FIT REQUISIO ["
//											+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1]
//											+ "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
							return chosen;
						}
					}
					// Cen�rio 3 - Direita - Reference < [0] [1]
					else if (band[0] >= reference && band[1] > reference && band[0] + numberOfSlots - 1 < t2) {
						if (chosenBandRight == null) {
							chosenBandRight = band.clone();
							chosenBandRight[1] = band[0] + numberOfSlots - 1;
						} else {
							if (band[0] - reference <= chosenBandRight[0] - reference) {
								chosenBandRight[0] = band[0];
								chosenBandRight[1] = band[0] + numberOfSlots - 1;
							}
						}
					}
				}
			}
			// Verificando se usar bloco da direita ou esquerda
			if (chosenBandLeft == null && chosenBandRight == null) {
				// System.out.println("LEFT AND RIGHT MIDDLE FIT NULL");
				return null;
			} else if (chosenBandLeft == null && chosenBandRight != null) {
				chosen = chosenBandRight.clone();
//				System.out.println(
//						"1 - CORE [" + circuit.getIndexCore() + "] ORANGE RIGHT REFERENCE FIRST FIT REQUISI ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "]");
				return chosen;
			} else if (chosenBandRight == null && chosenBandLeft != null) {
				chosen = chosenBandLeft.clone();
//				System.out
//						.println("1 - CORE [" + circuit.getIndexCore() + "] ORANGE LEFT REFERENCE LAST FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "]");
				return chosen;
			} else if (chosenBandLeft[1] - reference <= reference - chosenBandRight[0]) {
				chosen = chosenBandLeft.clone();
//				System.out
//						.println("1 - CORE [" + circuit.getIndexCore() + "] ORANGE LEFT REFERENCE LAST FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "]");
				return chosen;
			} else if (chosenBandLeft[1] - reference >= reference - chosenBandRight[0]) {
				chosen = chosenBandRight.clone();
//				System.out.println(
//						"1 - CORE [" + circuit.getIndexCore() + "] ORANGE RIGHT REFERENCE FIRST FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "]");
				return chosen;
			}
		}
		// Core 3 - 6 (GREEN) Last Fit
		else if (circuit.getIndexCore() == 3 || circuit.getIndexCore() == 6) {
			int band[] = null;
			for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
				band = freeSpectrumBands.get(i);
				if (band[1] - band[0] + 1 >= numberOfSlots) {
					// System.out.println("T3 = " + t3 + " BandaCompleta [" + band[0] + " - " +
					// band[1] + "] REQUISIO [" + numberOfSlots + "]");
					// Cen�rio 1 (Passa pelo T3) [0] < t3 < [1]
					if (band[0] <= t3 && band[1] >= t3 && (t3 - numberOfSlots) >= band[0] && t3 - numberOfSlots >= t2) {
						chosen = new int[2];
						chosen[1] = t3 - 1;
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println("1 - CORE [" + circuit.getIndexCore() + "] GREEN IN T3 REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//								+ band[0] + " - " + band[1] + "]");
						return chosen;
						// Cen�rio 2 (Antes de T3) [0] [1] < t3
					} else if (band[1] < t3 && band[1] - numberOfSlots + 1 >= t2) {
						chosen = band.clone();
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println("1 - CORE [" + circuit.getIndexCore() + "] GREEN BEFORE T3 REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//								+ band[0] + " - " + band[1] + "]");
						return chosen;
					}
				}
			}
		}
		// Core 0 CENTRAL(BLACK) Last Fit
		else if (circuit.getIndexCore() == 0) {
			int band[] = null;
			for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
				band = freeSpectrumBands.get(i);
				if (band[1] - band[0] + 1 >= numberOfSlots) {
					// Cenrio 1 - Passando Dentro de T3
					if (band[0] <= t3 && band[1] >= t3 && band[1] - numberOfSlots + 1 >= t3) {
						chosen = band.clone();
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println("1 - CORE [" + circuit.getIndexCore() + "] BLACK LAST IN FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//								+ band[0] + " - " + band[1] + "]");
						return chosen;
						// Cenrio 2 - Passando depois de T3
					} else if (band[0] >= t3) {
						chosen = band.clone();
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println("1 - CORE [" + circuit.getIndexCore() + "] BLACK LAST AFTER FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//								+ band[0] + " - " + band[1] + "]");
						return chosen;
					}
				}
			}
		}
		return null;
	}

	// Segunda tentativa de aloca
	public int[] policy2(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp) {
		// Verificando se requisi��o est� dentro dos limites de amplitude do sinal
		int maxAmplitude = circuit.getPair().getSource().getTxs().getMaxSpectralAmplitude();
		if (numberOfSlots > maxAmplitude)
			return null;
		// Setando Divisores de Espectro
		int maxSpectrumSize = circuit.getRoute().getLink(0).getCore(0).getNumOfSlots();
		int minSpectrumRange = maxSpectrumSize / COREQUANTITY;
		int t1 = minSpectrumRange * 2;
		int t2 = minSpectrumRange * 4;
		int t3 = minSpectrumRange * 6;
		// Escolhendo Espectro
		int chosen[] = null;

		// Core 1 - 4 (BLUE) First Fit Depois de T1
		if (circuit.getIndexCore() == 1 || circuit.getIndexCore() == 4) {
			for (int[] band : freeSpectrumBands) {
				if (band[1] - band[0] + 1 >= numberOfSlots) {
					// Cen�rio 1 (Passa pelo T1)
					if (band[0] <= t1 && band[1] >= t1 && (t1 + numberOfSlots - 1) < band[1]) {
						chosen = new int[2];
						chosen[0] = t1;
						chosen[1] = chosen[0] + numberOfSlots - 1;
//						System.out.println("2 - CORE [" + circuit.getIndexCore() + "] BLUE IN T1 FIST FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//								+ band[0] + " - " + band[1] + "]");
						return chosen;
					}
					// Cen�rio 2 (Depois do t1)
					else if (band[0] > t1) {
						chosen = band.clone();
						chosen[1] = chosen[0] + numberOfSlots - 1;
//						System.out.println("2 - CORE [" + circuit.getIndexCore()
//								+ "] BLUE AFTER F1 FIST FIT REQUISIO [" + numberOfSlots + "] ESPECTRO = [" + chosen[0]
//								+ " - " + chosen[1] + "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}
				}
			}
		}
		// Core 2 - 5 (ORANGE) Last Fit <- t1 | First Fit -> t2
		else if (circuit.getIndexCore() == 2 || circuit.getIndexCore() == 5) {
			// Primeiro Bloco Last Fit T1
			int band[] = null;
			// Percorrendo vetor de espectro livro de tr�s pra frente
			for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
				band = freeSpectrumBands.get(i);
				if (band[1] - band[0] + 1 >= numberOfSlots) {
					// Cen�rio 1 ( Passando por T1)
					if (band[0] <= t1 && band[1] >= t1 && (t1 - numberOfSlots) >= band[0]) {
						chosen = new int[2];
						chosen[1] = t1 - 1;
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println("2 - CORE [" + circuit.getIndexCore()
//								+ "] ORANGE IN T1 LAST FIT REQUISIO [" + numberOfSlots + "] ESPECTRO = [" + chosen[0]
//								+ " - " + chosen[1] + "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}
					// Cen�rio 2 (Antes de T1)
					else if (band[1] < t1) {
						chosen = band.clone();
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println(
//								"2 - CORE [" + circuit.getIndexCore() + "] ORANGE BEFORE T1 LAST FIT REQUISIO ["
//										+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1]
//										+ "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}

				}
			}
			// Segundo Bloco First Fit T2
			for (int[] band1 : freeSpectrumBands) {
				if (band1[1] - band1[0] + 1 >= numberOfSlots) {
					// Cen�rio 1 ( Passa pelo t2)
					if (band1[0] <= t2 && band1[1] >= t2 && (t2 + numberOfSlots - 1) <= band1[1]) {
						chosen = new int[2];
						chosen[0] = t2;
						chosen[1] = chosen[0] + numberOfSlots - 1;
//						System.out.println("2 - CORE [" + circuit.getIndexCore()
//								+ "] ORANGE IN T2 FIST FIT REQUISIO [" + numberOfSlots + "] ESPECTRO = [" + chosen[0]
//								+ " - " + chosen[1] + "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}
					// Cen�rio 2 (Depois de t2)
					else if (band1[0] >= t2) {
						chosen = band1.clone();
						chosen[1] = chosen[0] + numberOfSlots - 1;
//						System.out.println(
//								"2 - CORE [" + circuit.getIndexCore() + "] ORANGE AFTER T2 FIST FIT REQUISIO ["
//										+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1]
//										+ "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}
				}
			}
		}
		// Core 3 - 6 (GREEN) Last Fit <- t2 | First Fit -> t3
		else if (circuit.getIndexCore() == 3 || circuit.getIndexCore() == 6) {
			// Primeiro Bloco Last Fit T2
			int band[] = null;
			// Percorrendo vetor de espectro livro de tr�s pra frente
			for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
				band = freeSpectrumBands.get(i);
				if (band[1] - band[0] + 1 >= numberOfSlots) {
					// Cen�rio 1 ( Passando por T2)
					if (band[0] <= t2 && band[1] >= t2 && (t2 - numberOfSlots) >= band[0]) {
						chosen = new int[2];
						chosen[1] = t2 - 1;
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println("2 - CORE [" + circuit.getIndexCore() + "] GREEN IN T2 LAST FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//								+ band[0] + " - " + band[1] + "]");
						return chosen;
					}
					// Cen�rio 2 (Antes de T2)
					else if (band[1] < t2) {
						chosen = band.clone();
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println(
//								"2 - CORE [" + circuit.getIndexCore() + "] GREEN BEFORE T2 LAST FIT REQUISIO ["
//										+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1]
//										+ "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}

				}
			}
			// Segundo Bloco First Fit T3
			for (int[] band1 : freeSpectrumBands) {
				if (band1[1] - band1[0] + 1 >= numberOfSlots) {
					// Cen�rio 1 ( Passa pelo t3)
					if (band1[0] <= t3 && band1[1] >= t3 && (t3 + numberOfSlots - 1) <= band1[1]) {
						chosen = new int[2];
						chosen[0] = t3;
						chosen[1] = chosen[0] + numberOfSlots - 1;
//						System.out.println("2 - CORE [" + circuit.getIndexCore()
//								+ "] GREEN IN T3 FIRST FIT REQUISIO [" + numberOfSlots + "] ESPECTRO = [" + chosen[0]
//								+ " - " + chosen[1] + "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}
					// Cen�rio 2 (Depois de t3)
					else if (band1[0] >= t3) {
						chosen = band1.clone();
						chosen[1] = chosen[0] + numberOfSlots - 1;
//						System.out.println(
//								"2 - CORE [" + circuit.getIndexCore() + "] GREEN AFTER T3 FIRST FIT REQUISIO ["
//										+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1]
//										+ "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}
				}
			}
		}
		// Core 0 CENTRAL(BLACK) Last Fit <- t3
		else if (circuit.getIndexCore() == 0) {
			int band[] = null;
			for (int i = freeSpectrumBands.size() - 1; i >= 0; i--) {
				band = freeSpectrumBands.get(i);
				if (band[1] - band[0] + 1 >= numberOfSlots) {
					// Cen�rio 1 ( Passa pelo t3)
					if (band[0] <= t3 && band[1] >= t3 && (t3 - numberOfSlots) >= band[0]) {
						chosen = new int[2];
						chosen[1] = t3 - 1;
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println("2 - CORE [" + circuit.getIndexCore() + "] BLACK IN T3 LAST FIT REQUISIO ["
//								+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1] + "] BandaCompleta ["
//								+ band[0] + " - " + band[1] + "]");
						return chosen;
					}
					// Cen�rio 2 (Antes de t3)
					else if (band[1] < t3) {
						chosen = band.clone();
						chosen[0] = chosen[1] - numberOfSlots + 1;
//						System.out.println(
//								"2 - CORE [" + circuit.getIndexCore() + "] BLACK BEFORE T3 LAST FIT REQUISIO ["
//										+ numberOfSlots + "] ESPECTRO = [" + chosen[0] + " - " + chosen[1]
//										+ "] BandaCompleta [" + band[0] + " - " + band[1] + "]");
						return chosen;
					}
				}
			}
		}

		// Return null se n�o achar espectro dispon�vel.
		return null;
	}
}
