/*
 * Copyright (c) 2020, KISTERS AG, Germany.
 * All rights reserved.
 * Modification, redistribution and use in source and binary
 * forms, with or without modification, are not permitted
 * without prior written approval by the copyright holder.
 */

import client.FileHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/*
 *@author pmrachkovskaya
 */
public class FileHandlerTest {

  @Test
  public void changeAttributeInJson() throws IOException {
    FileHandler.changeOrganisationsAttribute("src/main/resources/configs/bank.json","5856e6cd-0da6-4573-9a04-cbb11f5e68d3","DE0355667",3);
  }
}
