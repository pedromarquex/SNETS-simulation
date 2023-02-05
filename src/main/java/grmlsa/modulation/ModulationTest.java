package grmlsa.modulation;

import grmlsa.Route;
import grmlsa.csa.CoreAndSpectrumAssignmentAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;

public class ModulationTest implements ModulationSelectionAlgorithmInterface{

	@Override
	public Modulation selectModulation(Circuit circuit, Route route,
			SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		// TODO Auto-generated method stub
		return cp.getMesh().getAvaliableModulations().get(1);
	}

	@Override
	public Modulation selectModulation(Circuit circuit, Route route,
			CoreAndSpectrumAssignmentAlgorithmInterface coreandspectrumAssignment, ControlPlane cp) {
		// TODO Auto-generated method stub
		return cp.getMesh().getAvaliableModulations().get(1);
	}

}
