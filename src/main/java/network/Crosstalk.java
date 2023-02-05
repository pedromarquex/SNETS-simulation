package network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import grmlsa.Route;
import grmlsa.modulation.Modulation;

/**
 * This class calcule crosstalk
 * reference -> [LOBATO et al 2019], [OLIVEIRA 2018]
 * 
 * @author jurandir
 *
 */
public class Crosstalk implements Serializable{
	public static final double PROPAGATIONCONSTANT = 10000000.0; // B in 1/m. Value from [LOBATO et al 2019]
	public static final double BENDINGRADIUS = 0.01; // R in m. Value from [LOBATO et al 2019]
	//public static final double COUPLINGCOEFFICIENTS = 0.0; // K in m-1. Value from [LOBATO et al 2019] sem xt
	//public static final double COUPLINGCOEFFICIENTS = 0.00584; // K in m-1. Value from [LOBATO et al 2019] baixo xt
	public static final double COUPLINGCOEFFICIENTS = 0.012; // K in m-1. Value from [LOBATO et al 2019] alto xt
	public static final double COREPITCH = 0.000045; // D in m. Value from [LOBATO et al 2019]
	public static final double SIGNALPOWER = 10.0; // The scenario that considers the same power for all lightpaths.
	
	//public static final double PROPAGATIONCONSTANT = 4000000.0; // B in 1/m. Value from [Klinkowski 2019]
	//public static final double BENDINGRADIUS = 0.05; // R in m. Value from [Klinkowski 2019]
	//public static final double COUPLINGCOEFFICIENTS = 0.00127; // K in m-1. Value from [Klinkowski 2019] 
	//public static final double COREPITCH = 0.00004; // D in m. Value from [Klinkowski 2019]
	//public static final double SIGNALPOWER = 10.0;
	
	//public static final double PROPAGATIONCONSTANT = 4000000.0; // B in 1/m. Value from [Su 2022]
	//public static final double BENDINGRADIUS = 0.05; // R in m. Value from [Su 2022]
	//public static final double COUPLINGCOEFFICIENTS = 0.0000127; // K in m-1. [Su 2022]
	//public static final double COREPITCH = 0.00004; // D in m. Value from [Su 2022]
	//public static final double SIGNALPOWER = 10.0;
		
	
//	public static final double XTBPSK = -16.0; //XT threshold levels, in dB. [OLIVEIRA 2018]
//	public static final double XTQPSK = -18.0; //XT threshold levels, in dB. [OLIVEIRA 2018]
//	public static final double XT8QAM = -21.0; //XT threshold levels, in dB. [OLIVEIRA 2018]
//	public static final double XT16QAM = -24.0; //XT threshold levels, in dB. [OLIVEIRA 2018]
//	public static final double XT32QAM = -28.0; //XT threshold levels, in dB. [OLIVEIRA 2018]
//	public static final double XT64QAM = -32.0; //XT threshold levels, in dB. [OLIVEIRA 2018]
	
//	public static final double XT4QAM = -20.7; //XT threshold levels, in dB. [LOBATO 2019]
//	public static final double XT8QAM = -24.78; //XT threshold levels, in dB. [LOBATO 2019]
//	public static final double XT16QAM = -27.36; //XT threshold levels, in dB. [LOBATO 2019]
//	public static final double XT32QAM = -30.39; //XT threshold levels, in dB. [LOBATO 2019]
//	public static final double XT64QAM = -33.29; //XT threshold levels, in dB. [LOBATO 2019]
	
//	public static final double XTBPSK = -14.0; //XT threshold levels, in dB. [Ehsani Moghaddam 2019]
//	public static final double XTQPSK = -18.5; //XT threshold levels, in dB. [Ehsani Moghaddam 2019]
//	public static final double XT8QAM = -21.0; //XT threshold levels, in dB. [Ehsani Moghaddam 2019]
//	public static final double XT16QAM = -25.0; //XT threshold levels, in dB. [Ehsani Moghaddam 2019]
//	public static final double XT32QAM = -27.0; //XT threshold levels, in dB. [Ehsani Moghaddam 2019]
//	public static final double XT64QAM = -34.0; //XT threshold levels, in dB. [Ehsani Moghaddam 2019]
	
	public static final double XTQPSK = -19.03; //XT threshold levels, in dB. [Jurandir] + outage probabilit FEC 1,3x10-2
	public static final double XT8QAM = -23.23; //XT threshold levels, in dB. [Jurandir] + outage probabilit FEC 1,3x10-2
	public static final double XT16QAM = -25.57; //XT threshold levels, in dB. [Jurandir] + outage probabilit FEC 1,3x10-2
	public static final double XT32QAM = -28.59; //XT threshold levels, in dB. [Jurandir] + outage probabilit FEC 1,3x10-2
	public static final double XT64QAM = -31.36; //XT threshold levels, in dB. [Jurandir] + outage probabilit FEC 1,3x10-2
	
	//public static final double XTBPSK = -21.7; //XT threshold levels, in dB. [Klinkowski and Zalewski, 2019] 
	//public static final double XTQPSK = -26.2; //XT threshold levels, in dB. [Klinkowski and Zalewski, 2019] 
	//public static final double XT8QAM = -28.7; //XT threshold levels, in dB. [Klinkowski and Zalewski, 2019] 
	//public static final double XT16QAM = -32.7; //XT threshold levels, in dB. [Klinkowski and Zalewski, 2019] 
	
	
	private double h;
	private double k;
	private int tipoEstimativa;
	
	public Crosstalk() {
		this.h = calculateH();
		this.k = calculateK();
		this.tipoEstimativa = 1; //1 para Lobato, 2 para Klinkowski, 3 para Klinkowski WCC-XT, 4 para Klinkowski WCF-XT
	}
	
	private double calculateK() {
		if(this.tipoEstimativa==1) {
			return 0; //não usa K
		}
		
		if(this.tipoEstimativa==2) {
			return 1; //para atualizar
		}
		
		if(this.tipoEstimativa==3) {
			return 3; //para atualizar
		}
				
		if(this.tipoEstimativa==4) {
			return 6;
		}
		
		return 0;
	}

	/**
	 * calculate h, from [LOBATO et al 2019]
	 * 
	 * @return h
	 */
	private double calculateH() {
		return ((2*COUPLINGCOEFFICIENTS*COUPLINGCOEFFICIENTS*BENDINGRADIUS)/(PROPAGATIONCONSTANT*COREPITCH));		
	}
	
	public double calculaCrosstalk(Circuit circuit) {
		Route rota = circuit.getRoute();
		double totalXT = 0;
		
		//Modelo Lobato
		if(this.tipoEstimativa==1) {
			for(Link link: rota.getLinkList()) {
				totalXT = totalXT + calculeCrosstalkInLink(circuit, link);
			}
		}
		
		// Modelo Klinkowski preciso
		if(this.tipoEstimativa==2) {
			for(Link link: rota.getLinkList()) {
				totalXT = totalXT + calculeCrosstalkInLink2(circuit, link);
			}
		}
		
		// Modelo Klinkowski WCC-XT
		if(this.tipoEstimativa==3) {
			for(Link link: rota.getLinkList()) {
				totalXT = totalXT + calculeCrosstalkInLink2(circuit, link);
			}
		}
		
		// Modelo Klinkowski WCF-XT
		if(this.tipoEstimativa==4) {
			for(Link link: rota.getLinkList()) {
				totalXT = totalXT + calculeCrosstalkInLink2(circuit, link);
			}
		}
		
		
		
		if(totalXT == 0) {
			totalXT =  0.000000000001; //the greatest value
		}
		
		
		return calculeLog(totalXT);
	}
	
	public boolean isAdmissible(Circuit circuit) {
		double xt = calculaCrosstalk(circuit);
		double xtThreshold = xtThreshold(circuit.getModulation());
		
		if(calculeLog(xt) > xtThreshold) {
			return false;
		}else {
			return true;
		}
	}
	
	public boolean isAdmissible(Circuit circuit, double xt) {
		double xtThreshold = xtThreshold(circuit.getModulation());
		
		if(xt > xtThreshold) {
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * Check XT on others
	 * 
	 * @param circuit
	 * @return Return true if the XT of other circuits is less than threshold
	 */
	public boolean isAdmissibleInOthers(Circuit circuit) {
		double xtJThreshold = xtThreshold(circuit.getModulation());
		double xtJ = 0.0;
		double nsoij = 0.0; // number of overlapping slots between i and j
		double nsj = 0.0; //number of slots of the connection j 
		double newXt = 0.0;
			
		ArrayList<Circuit> circuitosAdj = new ArrayList<Circuit>();		
		Route rota = circuit.getRoute();
		
		for(Link link: rota.getLinkList()) {
			ArrayList<Core> adjacentsCores = link.coresAdjacents(circuit.getIndexCore());			
			for(Core core : adjacentsCores) {		
				for(Circuit circuit2 : core.getCircuitList()) {
					if(isIntersection(circuit.getSpectrumAssigned(), circuit2.getSpectrumAssigned()) ) {
						if(!circuitosAdj.contains(circuit2)) {
							circuitosAdj.add(circuit2);
						}
					}
				}
			}
		}
		
		if (circuitosAdj.isEmpty()) {return true;}
				
		
		for (Circuit circuitNeighbor : circuitosAdj) {
			xtJ = circuitNeighbor.getXt();
			xtJThreshold = xtThreshold(circuitNeighbor.getModulation());
			
			//System.out.println(circuitNeighbor.getId()+"xt atual="+xtJ+"  limiar="+xtJThreshold);
			
			newXt = 0.0;
			nsoij = 0.0; 
			nsj = 0.0; 
			
			for (Link link : circuitNeighbor.route.getLinkList()) {
				if (circuit.getRoute().getLinkList().contains(link)) {
					if(isIntersection(circuit.getSpectrumAssigned(), circuitNeighbor.getSpectrumAssigned())) {
						//nsj = nsj + sizeSpectrumAllocate(circuitNeighbor.getSpectrumAssigned());
						nsj = nsj + sizeSpectrumAllocate(circuit.getSpectrumAssigned());
						nsoij = nsoij + numberOfOverlapping(circuit.getSpectrumAssigned(), circuitNeighbor.getSpectrumAssigned());
						//newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000));
						newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
					}
				}
			}
			
						
			//System.out.println(newXt);
			//newXt = xtJ - newXt;
			xtJ = Math.pow(10.0, (xtJ / 10.0));
			newXt = xtJ + newXt;
			newXt = calculeLog(newXt);
			

			if(newXt > xtJThreshold) {
				//System.out.println("bloqueio");
				return false;
			}				
		}		
		//System.out.println("NAO bloqueio");
		return true;
	}
	
	
	//Método para imprimir teste
	public boolean isAdmissibleInOthersImprimirLog(Circuit circuit) {
		double xtJThreshold = xtThreshold(circuit.getModulation());
		double xtJ = 0.0;
		double nsoij = 0.0; // number of overlapping slots between i and j
		double nsj = 0.0; //number of slots of the connection j 
		double newXt = 0.0;
			
		ArrayList<Circuit> circuitosAdj = new ArrayList<Circuit>();		
		Route rota = circuit.getRoute();
		
		for(Link link: rota.getLinkList()) {
			ArrayList<Core> adjacentsCores = link.coresAdjacents(circuit.getIndexCore());			
			for(Core core : adjacentsCores) {		
				for(Circuit circuit2 : core.getCircuitList()) {
					if(isIntersection(circuit.getSpectrumAssigned(), circuit2.getSpectrumAssigned())) {
						if(!circuitosAdj.contains(circuit2)) {
							circuitosAdj.add(circuit2);
						}
					}
				}
			}
		}
		
		if (circuitosAdj.isEmpty()) {return true;}
				
		for (Circuit circuitNeighbor : circuitosAdj) {
			//System.out.println(circuitNeighbor.getId());
			xtJ = circuitNeighbor.getXt();
			
			
			
			xtJThreshold = xtThreshold(circuitNeighbor.getModulation());
			
			//System.out.println(circuitNeighbor.getId()+"xt atual="+xtJ+"  limiar="+xtJThreshold);
			
			newXt = 0.0;
			nsoij = 0.0; 
			nsj = 0.0; 
			
			for (Link link : circuitNeighbor.route.getLinkList()) {
				if (circuit.getRoute().getLinkList().contains(link)) {
					if(isIntersection(circuit.getSpectrumAssigned(), circuitNeighbor.getSpectrumAssigned())) {
						//nsj = nsj + sizeSpectrumAllocate(circuitNeighbor.getSpectrumAssigned());
						nsj = nsj + sizeSpectrumAllocate(circuit.getSpectrumAssigned());
						nsoij = nsoij + numberOfOverlapping(circuit.getSpectrumAssigned(), circuitNeighbor.getSpectrumAssigned());
						//newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000));
						newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
					}
				}
			}
			
						
			//System.out.println(newXt);
			//newXt = xtJ - newXt;
			xtJ = Math.pow(10.0, (xtJ / 10.0));
			newXt = xtJ + newXt;
			newXt = calculeLog(newXt);
			
			//System.out.println(circuitNeighbor.getId()+"Novo xt ="+newXt+"  limiar="+xtJThreshold+"\n");
			
			//System.out.println("--O crosstalk no caminho óptico "+circuitNeighbor.getId()+", já ativo no núcleo "+circuitNeighbor.getIndexCore()+", após a alocação do caminho óptico "+circuit.getId()+" será de: "+newXt);
			
			System.out.println("--O crosstalk no caminho óptico "+circuitNeighbor.getId()+", já ativo no núcleo "+circuitNeighbor.getIndexCore()+", após a alocação do caminho óptico "+circuit.getId()+" será de: "+newXt+" (limiar: "+xtThreshold(circuitNeighbor.getModulation())+")");
			
			//if(newXt > xtJThreshold) {
			//	return false;
			//}				
		}		
		
		return true;
	}
	
	//Método para atualizar o valor de XT nos vizinhos do novo caminho óptico
	public void atualizaXTnosOutros(Circuit circuit) {
		double xtJThreshold = xtThreshold(circuit.getModulation());
		double xtJ = 0.0;
		double nsoij = 0.0; // number of overlapping slots between i and j
		double nsj = 0.0; //number of slots of the connection j 
		double newXt = 0.0;
			
		ArrayList<Circuit> circuitosAdj = new ArrayList();		
		Route rota = circuit.getRoute();
		
		for(Link link: rota.getLinkList()) {
			ArrayList<Core> adjacentsCores = link.coresAdjacents(circuit.getIndexCore());			
			for(Core core : adjacentsCores) {		
				for(Circuit circuit2 : core.getCircuitList()) {
					if(isIntersection(circuit.getSpectrumAssigned(), circuit2.getSpectrumAssigned())) {
						if(!circuitosAdj.contains(circuit2)) {
							circuitosAdj.add(circuit2);
						}
					}
				}
			}
		}
		
		//if (circuitosAdj.isEmpty()) {return void;}
				
		for (Circuit circuitNeighbor2 : circuitosAdj) {
			xtJ = circuitNeighbor2.getXt();
			xtJThreshold = xtThreshold(circuitNeighbor2.getModulation());
			
			//System.out.println(circuitNeighbor.getId()+"xt atual="+xtJ+"  limiar="+xtJThreshold);
			
			newXt = 0.0;
			nsoij = 0.0; 
			nsj = 0.0; 
			
			for (Link link : circuitNeighbor2.route.getLinkList()) {
				if (circuit.getRoute().getLinkList().contains(link)) {
					if(isIntersection(circuit.getSpectrumAssigned(), circuitNeighbor2.getSpectrumAssigned())) {
						//nsj = nsj + sizeSpectrumAllocate(circuitNeighbor.getSpectrumAssigned());
						nsj = nsj + sizeSpectrumAllocate(circuit.getSpectrumAssigned());
						nsoij = nsoij + numberOfOverlapping(circuit.getSpectrumAssigned(), circuitNeighbor2.getSpectrumAssigned());
						//newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000));
						newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
					}
				}
			}
			
						
			xtJ = Math.pow(10.0, (xtJ / 10.0));
			newXt = xtJ + newXt;
			newXt = calculeLog(newXt);
			
			circuitNeighbor2.setXt(newXt);
		}
		
	}
	
	//Método para atualizar o valor de XT nos vizinhos do novo caminho óptico
		public void atualizaXTnosOutrosRemocao(Circuit circuit) {
			double xtJThreshold = xtThreshold(circuit.getModulation());
			double xtJ = 0.0;
			double nsoij = 0.0; // number of overlapping slots between i and j
			double nsj = 0.0; //number of slots of the connection j 
			double newXt = 0.0;
				
			ArrayList<Circuit> circuitosAdj = new ArrayList();		
			Route rota = circuit.getRoute();
			
			for(Link link: rota.getLinkList()) {
				ArrayList<Core> adjacentsCores = link.coresAdjacents(circuit.getIndexCore());			
				for(Core core : adjacentsCores) {		
					for(Circuit circuit2 : core.getCircuitList()) {
						if(isIntersection(circuit.getSpectrumAssigned(), circuit2.getSpectrumAssigned())) {
							if(!circuitosAdj.contains(circuit2)) {
								circuitosAdj.add(circuit2);
								//System.out.println("-------------------");
							}
						}
					}
				}
			}
			
			//if (circuitosAdj.isEmpty()) {return void;}
			
			//System.out.println("\n----------------");
					
			for (Circuit circuitNeighbor2 : circuitosAdj) {
				xtJ = circuitNeighbor2.getXt();
				xtJThreshold = xtThreshold(circuitNeighbor2.getModulation());
				
				//System.out.println(circuitNeighbor.getId()+"xt atual="+xtJ+"  limiar="+xtJThreshold);
				
				newXt = 0.0;
				nsoij = 0.0; 
				nsj = 0.0; 
				
				for (Link link : circuitNeighbor2.route.getLinkList()) {
					if (circuit.getRoute().getLinkList().contains(link)) {
						if(isIntersection(circuit.getSpectrumAssigned(), circuitNeighbor2.getSpectrumAssigned())) {
							//nsj = nsj + sizeSpectrumAllocate(circuitNeighbor.getSpectrumAssigned());
							nsj = nsj + sizeSpectrumAllocate(circuit.getSpectrumAssigned());
							nsoij = nsoij + numberOfOverlapping(circuit.getSpectrumAssigned(), circuitNeighbor2.getSpectrumAssigned());
							//newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000));
							newXt = newXt + (((nsoij/nsj)*SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
						}
					}
				}
				
				//System.out.println("XT no caminho óptico "+circuitNeighbor2.getId()+" é igual a "+circuitNeighbor2.getXt() );
				
				xtJ = Math.pow(10.0, (xtJ / 10.0));
				newXt = xtJ - newXt;
				
				//System.out.println("\n\n\n\n"+newXt);
				if(newXt <= 0.00000001) {
					circuitNeighbor2.setXt(-120.0);
				}else {
					newXt = calculeLog(newXt);
					circuitNeighbor2.setXt(newXt);
				}
				
				//System.out.println("Após a remoção do caminho óptico "+circuit.getId()+", o XT no caminho óptico "+circuitNeighbor2.getId()+" passa a ser igual a "+circuitNeighbor2.getXt() );
			}
			
		}

	//OLIVEIRA 2018 and [Ehsani Moghaddam 2019]
	public double xtThreshold(Modulation modulation) {
		switch(modulation.getName()) {
		//	case "BPSK":
		//		return XTBPSK;
			case "QPSK":
				return XTQPSK;
			case "8QAM":
				return XT8QAM;
			case "16QAM":
				return XT16QAM;
			case "32QAM":
				return XT32QAM;
			case "64QAM":
				return XT64QAM;
			default:
				return 0.0;
		}
	}
	

	private double calculeLog(double valor) {
		return (10*(Math.log10(valor)));
	}
	
//	private double calculeIsoij(Circuit circuit) {
//		return 0.5;
//	}
	
	//
	// Modelo de Lobato et al 2019
	//
	private double calculeCrosstalkInLink(Circuit circuit, Link link) {
		
		double xtInLink = 0.0;
		ArrayList<Core> adjacentsCores = link.coresAdjacents(circuit.getIndexCore());
		
		for(Core core : adjacentsCores) {
			//xtInLink = xtInLink + ((SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
			xtInLink = xtInLink + ((calculeIsoij(circuit, core)*SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
		}
		
		return xtInLink;
	}
	
	//
	// Modelo tradicional Klinkowski 2019
	//
	private double calculeCrosstalkInLink2(Circuit circuit, Link link) {
		
		double xtInLink = 0.0;
		ArrayList<Core> adjacentsCores = link.coresAdjacents(circuit.getIndexCore());
		
		// Klinkowski 2019 estimativa precisa
		if(this.tipoEstimativa==2) {
			this.k = 1;
			boolean flag = false;
			
			//System.out.println("core: "+circuit.getIndexCore());
			
			for(Core core : adjacentsCores) {
				List<int[]> espectrosLivres = new ArrayList<>();
				espectrosLivres = core.getSpectrum().getFreeSpectrumBands();
				
				for(int[] i : espectrosLivres) {
					if(numberOfOverlapping(i, circuit.getSpectrumAssigned()) != (circuit.getSpectrumAssigned()[1] - circuit.getSpectrumAssigned()[0])+1) {
						flag = true;
						break;
					}
				}
				
				if(flag) {
					//System.out.println(core.getId());
					xtInLink = xtInLink + ((SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
				}
				
				//System.out.println(core.getId());
				//xtInLink = xtInLink + ((SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
				
			}
			//System.out.println("-------------");
		}
		
		// Klinkowski 2019 WCC-XT
		if(this.tipoEstimativa==3) {
			if(circuit.getIndexCore()==0) {
				this.k = 6;
			}else {
				this.k = 3;
			}
			
			for(int i=1; i<=k; i++) {
				//xtInLink = xtInLink + ((SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
				xtInLink = xtInLink + ((SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
			}
		}
		
		// Klinkowski 2019 WCF-XT
		if(this.tipoEstimativa==4) {
			this.k = 6;
			
			for(int i=1; i<=k; i++) {
				//xtInLink = xtInLink + ((SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
				xtInLink = xtInLink + ((SIGNALPOWER*h*link.getDistance()*1000)/SIGNALPOWER);
			}
		}
		
		
		
		return xtInLink;
	}
	
	//private double calculeCrosstalkInLink(Circuit circuit, Link link) {
	//	return (calculeIsoij(circuit)*SIGNALPOWER*h*link.getDistance())/SIGNALPOWER;
	//}
	
	
	private int sizeSpectrumAllocate(int[] spectrum) {
		return spectrum[1]-spectrum[0]+1;
	}
	
	private double calculeIsoij(Circuit circuit, Core core) {
		//int[] spectrum1 = circuit.getSpectrumAssigned();
		double nsoij = 0; // number of overlapping slots between i and j
		double nsj = 0; //number of slots of the connection j 
		int quantInter = 0;
		
		
		for(Circuit circuit2 : core.getCircuitList()) {
			if(isIntersection(circuit.getSpectrumAssigned(), circuit2.getSpectrumAssigned())) {
				nsj = nsj + sizeSpectrumAllocate(circuit2.getSpectrumAssigned());
				nsoij = nsoij + numberOfOverlapping(circuit.getSpectrumAssigned(), circuit2.getSpectrumAssigned());
				quantInter++;
				//nsoij = nsoij + numberOfOverlapping(spectrum1, circuit2.getSpectrumAssigned());
			}
		}
		

		
		if(quantInter==0) {
			return 0;
		}else {
			return nsoij/nsj;
		}
	}

	private boolean isIntersection(int[] spectrum1, int[] spectrum2) {
		ArrayList<Integer> slots2 = new ArrayList<Integer>();
		
		for(int i=spectrum2[0]; i<=spectrum2[1]; i++) {
			slots2.add(i);
		}
				
		for(int i=spectrum1[0]; i<=spectrum1[1]; i++) {
			if(slots2.contains(i)) {
				return true;
			}
		}
		
		return false;
	}
	
	private int numberOfOverlapping(int[] spectrum1, int[] spectrum2) {
		int numberOfOverlapping = 0;
		
		for(int i=spectrum1[0]; i<=spectrum1[1]; i++) {
			if ((i>=spectrum2[0]) && (i<=spectrum2[1])) {
				numberOfOverlapping++;
			}
		}
		//System.out.println(numberOfOverlapping);
				
		return numberOfOverlapping;
	}
	
	private void imprimeTeste(Circuit circuit, String origem, String destino, double totalXT) {
		if (circuit.getSource().getName().equals(origem) && circuit.getDestination().getName().equals(destino)) {
			System.out.println("\n\n");
			for(Link link : circuit.getRoute().getLinkList()) {
				System.out.println("Link :"+link.getSource().getName()+"-"+link.getDestination().getName());
				for(Core core : link.getCores()) {
					System.out.print("Core: "+core.getId());
					for(int[] i : core.getFreeSpectrumBands(circuit.getGuardBand())) {
						System.out.print("||"+i[0]+"-"+i[1]);
					}
					System.out.println("");
				}
			}
			System.out.println("Crosstalk para o circuito tendo o core: "+circuit.getIndexCore()+" e slots: "+circuit.getSpectrumAssigned()[0]+"-"+circuit.getSpectrumAssigned()[1]+" eh "+calculeLog(totalXT)+" Com mudulação: "+circuit.getModulation().getName()+" portanto: "+isAdmissible(circuit, calculeLog(totalXT)));
			//System.out.println("Crosstalk para o circuito: "+circuit.getSource().getName()+"-"+circuit.getDestination().getName()+" tendo o core: "+circuit.getIndexCore()+" e slots: "+circuit.getSpectrumAssigned()[0]+"-"+circuit.getSpectrumAssigned()[1]+" eh "+calculeLog(totalXT)+" Com mudulação: "+circuit.getModulation().getName()+" portanto: "+isAdmissible(circuit, calculeLog(totalXT)));
		
		}
	}
	
	public double getH() {
		return h;
	}
}
