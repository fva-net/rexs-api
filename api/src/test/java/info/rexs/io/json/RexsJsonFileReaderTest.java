/*******************************************************************************
 * Copyright (C) 2020 FVA GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package info.rexs.io.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import info.rexs.db.constants.RexsVersion;
import info.rexs.io.RexsIoException;
import info.rexs.io.json.jsonmodel.JSONModel;
import info.rexs.model.RexsModel;

public class RexsJsonFileReaderTest {

	@Test
	public void readRawModel_nonExistingFileThrowsFileNotFoundException() throws Exception {
		Path nonExistingFilePath = Paths.get("path/to/non/existing/file");

		assertThatExceptionOfType(RexsIoException.class).isThrownBy(() -> {
			RexsJsonFileReader reader = new RexsJsonFileReader(nonExistingFilePath);
			reader.readRawModel();
		})
		.withMessageEndingWith("does not exist");
	}

	@Test
	public void readRawModel_invalidRexsFileThrowsJAXBException() throws Exception {
		String invalidRexsFileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><valid xml=\"file\"/>";
		Path invalidRexsFilePath = Paths.get("target").resolve("invalid-rexs-file-" + System.currentTimeMillis() + ".rexsj");
		Files.write(invalidRexsFilePath,
				invalidRexsFileContent.getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING);

		assertThatExceptionOfType(RexsIoException.class).isThrownBy(() -> {
			RexsJsonFileReader reader = new RexsJsonFileReader(invalidRexsFilePath);
			reader.readRawModel();
		});
	}

	@Test
	public void readRawModel_readsExsistingRexsFile() throws Exception {
		Path rexsFilePath = Paths.get("src/test/resources").resolve("FVA_Planetary_stage_-_Minus_gearing_1.1.rexsj");
		RexsJsonFileReader reader = new RexsJsonFileReader(rexsFilePath);
		JSONModel rawModel = reader.readRawModel();

		assertThat(rawModel).isNotNull();
		assertThat(rawModel.getModel().getVersion()).isEqualTo(RexsVersion.V1_1.getName());
		assertThat(rawModel.getModel().getComponents()).hasSize(97);
		assertThat(rawModel.getModel().getRelations()).hasSize(140);
	}

	@Test
	public void read_readsExsistingRexsFile() throws Exception {
		Path rexsFilePath = Paths.get("src/test/resources").resolve("FVA_Planetary_stage_-_Minus_gearing_1.1.rexsj");
		RexsJsonFileReader reader = new RexsJsonFileReader(rexsFilePath);
		RexsModel rexsModel = reader.read();

		assertThat(rexsModel).isNotNull();
		assertThat(rexsModel.getComponents()).hasSize(97);
	}

	@Test
	public void read_readsExsistingRexsFileWithFile() throws Exception {
		File rexsFile = new File("src/test/resources/FVA_Planetary_stage_-_Minus_gearing_1.1.rexsj");
		RexsJsonFileReader reader = new RexsJsonFileReader(rexsFile);
		RexsModel rexsModel = reader.read();

		assertThat(rexsModel).isNotNull();
		assertThat(rexsModel.getComponents()).hasSize(97);
	}

	@Test
	public void read_readsExsistingRexsFileWithString() throws Exception {
		String rexsFileStringPath = "src/test/resources/FVA_Planetary_stage_-_Minus_gearing_1.1.rexsj";
		RexsJsonFileReader reader = new RexsJsonFileReader(rexsFileStringPath);
		RexsModel rexsModel = reader.read();

		assertThat(rexsModel).isNotNull();
		assertThat(rexsModel.getComponents()).hasSize(97);
	}
}