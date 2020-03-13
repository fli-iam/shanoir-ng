package org.shanoir.uploader.model.rest;

import org.apache.log4j.Logger;

public final class PseudonymusHashValuesMapper {

	private static Logger logger = Logger.getLogger(PseudonymusHashValuesMapper.class);

	public static PseudonymusHashValues generatedToLocal(
			org.shanoir.ws.generated.uploader.PseudonymusHashValues phvRemote) {
		if (phvRemote != null) {
			PseudonymusHashValues pHv = new PseudonymusHashValues();
			pHv.setId(Long.parseLong(phvRemote.getId(), 10));
			pHv.setBirthDateHash(phvRemote.getBirthDateHash());
			pHv.setBirthNameHash1(phvRemote.getBirthNameHash1());
			pHv.setBirthNameHash2(phvRemote.getBirthNameHash2());
			pHv.setBirthNameHash3(phvRemote.getBirthNameHash3());
			pHv.setFirstNameHash1(phvRemote.getFirstNameHash1());
			pHv.setFirstNameHash2(phvRemote.getFirstNameHash2());
			pHv.setFirstNameHash3(phvRemote.getFirstNameHash3());
			pHv.setLastNameHash1(phvRemote.getLastNameHash1());
			pHv.setLastNameHash2(phvRemote.getLastNameHash2());
			pHv.setLastNameHash3(phvRemote.getLastNameHash3());
			return pHv;
		}
		return null;

	}

	public static org.shanoir.ws.generated.uploader.PseudonymusHashValues localToGenerated(PseudonymusHashValues pHv) {
		if (pHv != null) {
			org.shanoir.ws.generated.uploader.PseudonymusHashValues phvRemote = new org.shanoir.ws.generated.uploader.PseudonymusHashValues();
			phvRemote.setBirthDateHash(pHv.getBirthDateHash());
			phvRemote.setBirthNameHash1(pHv.getBirthNameHash1());
			phvRemote.setBirthNameHash2(pHv.getBirthNameHash2());
			phvRemote.setBirthNameHash3(pHv.getBirthNameHash3());
			phvRemote.setFirstNameHash1(pHv.getFirstNameHash1());
			phvRemote.setFirstNameHash2(pHv.getFirstNameHash2());
			phvRemote.setFirstNameHash3(pHv.getFirstNameHash3());
			phvRemote.setLastNameHash1(pHv.getLastNameHash1());
			phvRemote.setLastNameHash2(pHv.getLastNameHash2());
			phvRemote.setLastNameHash3(pHv.getLastNameHash3());
			return phvRemote;
		}
		return null;
	}

}
