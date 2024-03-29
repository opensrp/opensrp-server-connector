package org.opensrp.connector.openmrs.service.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.api.domain.Location;
import org.opensrp.api.util.LocationTree;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;

import java.io.IOException;

public class OpenmrsLocationTest extends OpenmrsApiService {

    OpenmrsLocationService ls;

    public OpenmrsLocationTest() throws IOException {
        super();
    }

    @Before
    public void setup() throws IOException {
        ls = new OpenmrsLocationService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword);
    }

    @Test
    public void testLocationTree() throws JSONException {
        LocationTree ltree = new Gson().fromJson(
                "{\"locationsHierarchy\":{\"map\":{\"215caa30-1906-4210-8294-23eb7914c1dd\":{\"id\":\"215caa30-1906-4210-8294-23eb7914c1dd\",\"label\":\"3-KHA\",\"node\":{\"locationId\":\"215caa30-1906-4210-8294-23eb7914c1dd\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\":{\"id\":\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\",\"label\":\"DURGAPUR\",\"node\":{\"locationId\":\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\",\"name\":\"DURGAPUR\",\"parentLocation\":{\"locationId\":\"215caa30-1906-4210-8294-23eb7914c1dd\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"215caa30-1906-4210-8294-23eb7914c1dd\"}},\"parent\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\"},\"429feb8b-0b8d-4496-8e54-fdc94affed07\":{\"id\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"label\":\"1-KHA\",\"node\":{\"locationId\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"name\":\"1-KHA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\":{\"id\":\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\",\"label\":\"Chapadaha Mauza\",\"node\":{\"locationId\":\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\",\"name\":\"Chapadaha Mauza\",\"parentLocation\":{\"locationId\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"name\":\"1-KHA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\"},\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\":{\"id\":\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\",\"label\":\"Kuptala-1-KHA\",\"node\":{\"locationId\":\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\",\"name\":\"Kuptala-1-KHA\",\"parentLocation\":{\"locationId\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"name\":\"1-KHA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"429feb8b-0b8d-4496-8e54-fdc94affed07\"}},\"parent\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\"},\"f2f803d5-857a-42a4-a05b-142c3327b4fc\":{\"id\":\"f2f803d5-857a-42a4-a05b-142c3327b4fc\",\"label\":\"SONORAY\",\"node\":{\"locationId\":\"f2f803d5-857a-42a4-a05b-142c3327b4fc\",\"name\":\"SONORAY\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\":{\"id\":\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\",\"label\":\"SARBANANDA\",\"node\":{\"locationId\":\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\",\"name\":\"SARBANANDA\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\":{\"id\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"label\":\"2-KHA\",\"node\":{\"locationId\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"name\":\"2-KHA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"3a041478-5d39-4d42-b785-67c2ae56febb\":{\"id\":\"3a041478-5d39-4d42-b785-67c2ae56febb\",\"label\":\"Kuptala-2KHA\",\"node\":{\"locationId\":\"3a041478-5d39-4d42-b785-67c2ae56febb\",\"name\":\"Kuptala-2KHA\",\"parentLocation\":{\"locationId\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"name\":\"2-KHA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\"},\"27e6d636-0683-4539-90b8-2c795318dc08\":{\"id\":\"27e6d636-0683-4539-90b8-2c795318dc08\",\"label\":\"BERADANGA\",\"node\":{\"locationId\":\"27e6d636-0683-4539-90b8-2c795318dc08\",\"name\":\"BERADANGA\",\"parentLocation\":{\"locationId\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\",\"name\":\"2-KHA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\"}},\"parent\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\"},\"5d0661b5-4868-49eb-a697-e4dc4348dfab\":{\"id\":\"5d0661b5-4868-49eb-a697-e4dc4348dfab\",\"label\":\"SHANTIRAM\",\"node\":{\"locationId\":\"5d0661b5-4868-49eb-a697-e4dc4348dfab\",\"name\":\"SHANTIRAM\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\":{\"id\":\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\",\"label\":\"SRIPUR\",\"node\":{\"locationId\":\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\",\"name\":\"SRIPUR\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"42423d74-a061-463b-93a1-2f773f0aae21\":{\"id\":\"42423d74-a061-463b-93a1-2f773f0aae21\",\"label\":\"1-KA\",\"node\":{\"locationId\":\"42423d74-a061-463b-93a1-2f773f0aae21\",\"name\":\"1-KA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\":{\"id\":\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\",\"label\":\"Kuptala-1-KA\",\"node\":{\"locationId\":\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\",\"name\":\"Kuptala-1-KA\",\"parentLocation\":{\"locationId\":\"42423d74-a061-463b-93a1-2f773f0aae21\",\"name\":\"1-KA\",\"parentLocation\":{\"locationId\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\",\"name\":\"Ward-1\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"42423d74-a061-463b-93a1-2f773f0aae21\"}},\"parent\":\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\"},\"dff51374-be72-46cb-a9a3-c7989e24430c\":{\"id\":\"dff51374-be72-46cb-a9a3-c7989e24430c\",\"label\":\"DHOPADANGA\",\"node\":{\"locationId\":\"dff51374-be72-46cb-a9a3-c7989e24430c\",\"name\":\"DHOPADANGA\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e8e88d43-e181-42f1-9de5-143149922eea\":{\"id\":\"e8e88d43-e181-42f1-9de5-143149922eea\",\"label\":\"RAMJIBAN\",\"node\":{\"locationId\":\"e8e88d43-e181-42f1-9de5-143149922eea\",\"name\":\"RAMJIBAN\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\":{\"id\":\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\",\"label\":\"KANCHIBARI\",\"node\":{\"locationId\":\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\",\"name\":\"KANCHIBARI\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"fa32786b-4063-4f39-b72d-a5bc0e549193\":{\"id\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\",\"label\":\"3-KA\",\"node\":{\"locationId\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"f872c792-32ac-49e7-a386-f6b968968ef1\":{\"id\":\"f872c792-32ac-49e7-a386-f6b968968ef1\",\"label\":\"Kuptala-3-KA\",\"node\":{\"locationId\":\"f872c792-32ac-49e7-a386-f6b968968ef1\",\"name\":\"Kuptala-3-KA\",\"parentLocation\":{\"locationId\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"fa32786b-4063-4f39-b72d-a5bc0e549193\"}},\"parent\":\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\"},\"765cb701-9e61-4ead-afb9-a63c943f4f14\":{\"id\":\"765cb701-9e61-4ead-afb9-a63c943f4f14\",\"label\":\"Korangi\",\"node\":{\"locationId\":\"765cb701-9e61-4ead-afb9-a63c943f4f14\",\"name\":\"Korangi\",\"tags\":[\"Town\"],\"voided\":false}},\"a57cef08-b47e-4b59-acd8-354279a63027\":{\"id\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"label\":\"3-KA\",\"node\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"name\":\"NALDANGA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"f30df310-0d30-4482-8dfe-667def649c20\":{\"id\":\"f30df310-0d30-4482-8dfe-667def649c20\",\"label\":\"PROTAP - MANDUAR PARA\",\"node\":{\"locationId\":\"f30df310-0d30-4482-8dfe-667def649c20\",\"name\":\"PROTAP - MANDUAR PARA\",\"parentLocation\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"a57cef08-b47e-4b59-acd8-354279a63027\"},\"f6933584-9248-409d-b06a-0988c470ce45\":{\"id\":\"f6933584-9248-409d-b06a-0988c470ce45\",\"label\":\"PROTAP - FUL PARA\",\"node\":{\"locationId\":\"f6933584-9248-409d-b06a-0988c470ce45\",\"name\":\"PROTAP - FUL PARA\",\"parentLocation\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"a57cef08-b47e-4b59-acd8-354279a63027\"},\"bac5a3b2-456f-4500-93a7-7a24be91909e\":{\"id\":\"bac5a3b2-456f-4500-93a7-7a24be91909e\",\"label\":\"PROTAP - KATA PROTAP\",\"node\":{\"locationId\":\"bac5a3b2-456f-4500-93a7-7a24be91909e\",\"name\":\"PROTAP - KATA PROTAP\",\"parentLocation\":{\"locationId\":\"a57cef08-b47e-4b59-acd8-354279a63027\",\"name\":\"3-KA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"a57cef08-b47e-4b59-acd8-354279a63027\"}},\"parent\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\"},\"f4e3cb47-fea1-418c-9a63-26374e424043\":{\"id\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"label\":\"RANGPUR\",\"node\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"tags\":[\"Division\"],\"voided\":false},\"children\":{\"a556070e-cd96-49bc-b079-2a415d476a97\":{\"id\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"label\":\"GAIBANDHA\",\"node\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"parentLocation\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"voided\":false},\"tags\":[\"District\"],\"voided\":false},\"children\":{\"960ada36-be32-4867-a0aa-b7f4b835c61f\":{\"id\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"label\":\"SADULLAPUR\",\"node\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"parentLocation\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"voided\":false},\"voided\":false},\"tags\":[\"Upazilla\"],\"voided\":false},\"children\":{\"bd57db27-71b9-467e-9503-ce2dec74e61b\":{\"id\":\"bd57db27-71b9-467e-9503-ce2dec74e61b\",\"label\":\"JAMALPUR\",\"node\":{\"locationId\":\"bd57db27-71b9-467e-9503-ce2dec74e61b\",\"name\":\"JAMALPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\":{\"id\":\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\",\"label\":\"FARIDPUR\",\"node\":{\"locationId\":\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\",\"name\":\"FARIDPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\":{\"id\":\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\",\"label\":\"KUMARPARA\",\"node\":{\"locationId\":\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\",\"name\":\"KUMARPARA\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"07b798a0-2219-4447-8b72-2510c0526a15\":{\"id\":\"07b798a0-2219-4447-8b72-2510c0526a15\",\"label\":\"DAMODARPUR\",\"node\":{\"locationId\":\"07b798a0-2219-4447-8b72-2510c0526a15\",\"name\":\"DAMODARPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\":{\"id\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"label\":\"NALDANGA\",\"node\":{\"locationId\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"name\":\"NALDANGA\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"},\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\":{\"id\":\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\",\"label\":\"RASULPUR\",\"node\":{\"locationId\":\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\",\"name\":\"RASULPUR\",\"parentLocation\":{\"locationId\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"name\":\"SADULLAPUR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"960ada36-be32-4867-a0aa-b7f4b835c61f\"}},\"parent\":\"a556070e-cd96-49bc-b079-2a415d476a97\"},\"57b34716-c291-4ca4-a7c8-28e65ab8819a\":{\"id\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"label\":\"GAIBANDHA SADAR\",\"node\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"parentLocation\":{\"locationId\":\"f4e3cb47-fea1-418c-9a63-26374e424043\",\"name\":\"RANGPUR\",\"voided\":false},\"voided\":false},\"tags\":[\"Upazilla\"],\"voided\":false},\"children\":{\"7491ac95-05d2-49a8-b6a9-463f357171eb\":{\"id\":\"7491ac95-05d2-49a8-b6a9-463f357171eb\",\"label\":\"LAKSHMIPUR\",\"node\":{\"locationId\":\"7491ac95-05d2-49a8-b6a9-463f357171eb\",\"name\":\"LAKSHMIPUR\",\"parentLocation\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"},\"725658c6-4d94-4791-bad6-614dec63d83b\":{\"id\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"label\":\"KUPTALA\",\"node\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"parentLocation\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"},\"d658d99a-1941-406b-bbdc-b46a2545de92\":{\"id\":\"d658d99a-1941-406b-bbdc-b46a2545de92\",\"label\":\"MALIBARI\",\"node\":{\"locationId\":\"d658d99a-1941-406b-bbdc-b46a2545de92\",\"name\":\"MALIBARI\",\"parentLocation\":{\"locationId\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\",\"name\":\"GAIBANDHA SADAR\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"}},\"parent\":\"a556070e-cd96-49bc-b079-2a415d476a97\"}},\"parent\":\"f4e3cb47-fea1-418c-9a63-26374e424043\"}}},\"cd4ed528-87cd-42ee-a175-5e7089521ebd\":{\"id\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"label\":\"Pakistan\",\"node\":{\"locationId\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"name\":\"Pakistan\",\"tags\":[\"Country\"],\"voided\":false},\"children\":{\"461f2be7-c95d-433c-b1d7-c68f272409d7\":{\"id\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"label\":\"Sindh\",\"node\":{\"locationId\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"name\":\"Sindh\",\"parentLocation\":{\"locationId\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"name\":\"Pakistan\",\"voided\":false},\"tags\":[\"Province\"],\"voided\":false},\"children\":{\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\":{\"id\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\",\"label\":\"Karachi\",\"node\":{\"locationId\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\",\"name\":\"Karachi\",\"parentLocation\":{\"locationId\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"name\":\"Sindh\",\"parentLocation\":{\"locationId\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\",\"name\":\"Pakistan\",\"voided\":false},\"voided\":false},\"tags\":[\"City\"],\"voided\":false},\"children\":{\"60c21502-fec1-40f5-b77d-6df3f92771ce\":{\"id\":\"60c21502-fec1-40f5-b77d-6df3f92771ce\",\"label\":\"Baldia\",\"node\":{\"locationId\":\"60c21502-fec1-40f5-b77d-6df3f92771ce\",\"name\":\"Baldia\",\"parentLocation\":{\"locationId\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\",\"name\":\"Karachi\",\"parentLocation\":{\"locationId\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\",\"name\":\"Sindh\",\"voided\":false},\"voided\":false},\"tags\":[\"Town\"],\"attributes\":{\"at1\":\"atttt1\"},\"voided\":false},\"parent\":\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\"}},\"parent\":\"461f2be7-c95d-433c-b1d7-c68f272409d7\"}},\"parent\":\"cd4ed528-87cd-42ee-a175-5e7089521ebd\"}}},\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\":{\"id\":\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\",\"label\":\"BAMANDANGA\",\"node\":{\"locationId\":\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\",\"name\":\"BAMANDANGA\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"e1f223f5-a59e-4a54-b44e-472ff2438684\":{\"id\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"label\":\"2-KA\",\"node\":{\"locationId\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"name\":\"2-KA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"parentLocation\":{\"locationId\":\"725658c6-4d94-4791-bad6-614dec63d83b\",\"name\":\"KUPTALA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\":{\"id\":\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\",\"label\":\"RAMPRASHAD\",\"node\":{\"locationId\":\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\",\"name\":\"RAMPRASHAD\",\"parentLocation\":{\"locationId\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"name\":\"2-KA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\"},\"36fc5398-8e7a-430b-ab3b-557788b4d89f\":{\"id\":\"36fc5398-8e7a-430b-ab3b-557788b4d89f\",\"label\":\"Kuptala-2-KA\",\"node\":{\"locationId\":\"36fc5398-8e7a-430b-ab3b-557788b4d89f\",\"name\":\"Kuptala-2-KA\",\"parentLocation\":{\"locationId\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"name\":\"2-KA\",\"parentLocation\":{\"locationId\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\",\"name\":\"Ward-2\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"e1f223f5-a59e-4a54-b44e-472ff2438684\"}},\"parent\":\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\"},\"774bca32-01ab-4c7a-91f0-b5c51c41945a\":{\"id\":\"774bca32-01ab-4c7a-91f0-b5c51c41945a\",\"label\":\"CHHAPARHATI\",\"node\":{\"locationId\":\"774bca32-01ab-4c7a-91f0-b5c51c41945a\",\"name\":\"CHHAPARHATI\",\"parentLocation\":{\"locationId\":\"11eaac2c-12d6-4958-b548-2d6768776b10\",\"name\":\"SUNDARGANJ\",\"parentLocation\":{\"locationId\":\"a556070e-cd96-49bc-b079-2a415d476a97\",\"name\":\"GAIBANDHA\",\"voided\":false},\"voided\":false},\"tags\":[\"Union\"],\"voided\":false},\"parent\":\"11eaac2c-12d6-4958-b548-2d6768776b10\"},\"f332d8ac-e57f-49ba-8fb0-c428651697a2\":{\"id\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"label\":\"3-KHA\",\"node\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"parentLocation\":{\"locationId\":\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"name\":\"NALDANGA\",\"voided\":false},\"voided\":false},\"tags\":[\"Unit\"],\"voided\":false},\"children\":{\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\":{\"id\":\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\",\"label\":\"DASLIA - ALL PARAS\",\"node\":{\"locationId\":\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\",\"name\":\"DASLIA - ALL PARAS\",\"parentLocation\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"},\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\":{\"id\":\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\",\"label\":\"PROTAP - OPADANI PARA\",\"node\":{\"locationId\":\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\",\"name\":\"PROTAP - OPADANI PARA\",\"parentLocation\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"},\"80efdc06-59b7-4594-bf24-561a7eb12676\":{\"id\":\"80efdc06-59b7-4594-bf24-561a7eb12676\",\"label\":\"PROTAP - SARDAR PARA\",\"node\":{\"locationId\":\"80efdc06-59b7-4594-bf24-561a7eb12676\",\"name\":\"PROTAP - SARDAR PARA\",\"parentLocation\":{\"locationId\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\",\"name\":\"3-KHA\",\"parentLocation\":{\"locationId\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\",\"name\":\"Ward-3\",\"voided\":false},\"voided\":false},\"tags\":[\"Mauza\"],\"voided\":false},\"parent\":\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"}},\"parent\":\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\"}},\"parentChildren\":{\"215caa30-1906-4210-8294-23eb7914c1dd\":[\"4ccd5a33-c462-4b53-b8c1-a1ad1c3ba0cf\"],\"318e5671-368b-4e9c-8bc1-7a6fb545c1e5\":[\"e1f223f5-a59e-4a54-b44e-472ff2438684\",\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\"],\"429feb8b-0b8d-4496-8e54-fdc94affed07\":[\"9047a5e3-66cf-4f83-b0b6-3cdd3d611272\",\"a8b7d760-0e7e-4fdb-9450-b41d31d1ec34\"],\"e8964ad4-e6f2-4aff-bb61-28c08d01af51\":[\"3a041478-5d39-4d42-b785-67c2ae56febb\",\"27e6d636-0683-4539-90b8-2c795318dc08\"],\"f6b22dad-75c4-47e6-923a-3d0a005ed8a7\":[\"a57cef08-b47e-4b59-acd8-354279a63027\",\"f332d8ac-e57f-49ba-8fb0-c428651697a2\"],\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\":[\"60c21502-fec1-40f5-b77d-6df3f92771ce\"],\"42423d74-a061-463b-93a1-2f773f0aae21\":[\"88abc9f1-d698-41e3-8e2d-0c900b16dfe6\"],\"fa32786b-4063-4f39-b72d-a5bc0e549193\":[\"f872c792-32ac-49e7-a386-f6b968968ef1\"],\"a57cef08-b47e-4b59-acd8-354279a63027\":[\"f30df310-0d30-4482-8dfe-667def649c20\",\"f6933584-9248-409d-b06a-0988c470ce45\",\"bac5a3b2-456f-4500-93a7-7a24be91909e\"],\"cd4ed528-87cd-42ee-a175-5e7089521ebd\":[\"461f2be7-c95d-433c-b1d7-c68f272409d7\"],\"f4e3cb47-fea1-418c-9a63-26374e424043\":[\"a556070e-cd96-49bc-b079-2a415d476a97\"],\"e1f223f5-a59e-4a54-b44e-472ff2438684\":[\"fbe4c8cf-5d52-4bc3-a4ec-9dcc1f5504cd\",\"36fc5398-8e7a-430b-ab3b-557788b4d89f\"],\"bfeb65bd-bff0-41bb-81a0-0220a4200bff\":[\"429feb8b-0b8d-4496-8e54-fdc94affed07\",\"42423d74-a061-463b-93a1-2f773f0aae21\"],\"11eaac2c-12d6-4958-b548-2d6768776b10\":[\"dff51374-be72-46cb-a9a3-c7989e24430c\",\"e8e88d43-e181-42f1-9de5-143149922eea\",\"d3367458-f5e5-4039-b1e7-f087cc5be3fa\",\"96cd1c2a-f678-4687-bd87-8f4c5eae261a\",\"f2f803d5-857a-42a4-a05b-142c3327b4fc\",\"e0d50bc5-09b2-4102-809d-687fe71d5fd0\",\"774bca32-01ab-4c7a-91f0-b5c51c41945a\",\"5d0661b5-4868-49eb-a697-e4dc4348dfab\",\"f48a6482-2ffd-4596-8d9b-46dadc3c73df\"],\"461f2be7-c95d-433c-b1d7-c68f272409d7\":[\"a529e2fc-6f0d-4e60-a5df-789fe17cca48\"],\"a556070e-cd96-49bc-b079-2a415d476a97\":[\"960ada36-be32-4867-a0aa-b7f4b835c61f\",\"57b34716-c291-4ca4-a7c8-28e65ab8819a\"],\"960ada36-be32-4867-a0aa-b7f4b835c61f\":[\"bd57db27-71b9-467e-9503-ce2dec74e61b\",\"1b93c923-5ebb-4c0a-8bbb-067cc5fc5c9f\",\"a39ce1d7-d8ee-49e9-8a81-02f7949f5ff0\",\"b25f114e-22e4-4cf8-89ef-af94ea2cecc5\",\"07b798a0-2219-4447-8b72-2510c0526a15\",\"e7d39ba2-45a1-498c-bcc5-937f179d81fa\"],\"57b34716-c291-4ca4-a7c8-28e65ab8819a\":[\"7491ac95-05d2-49a8-b6a9-463f357171eb\",\"725658c6-4d94-4791-bad6-614dec63d83b\",\"d658d99a-1941-406b-bbdc-b46a2545de92\"],\"f332d8ac-e57f-49ba-8fb0-c428651697a2\":[\"2fc43738-ace5-4961-8e8f-ab7d00e5bc63\",\"50d3dddd-9fba-4895-9b96-fe66d42e6fed\",\"80efdc06-59b7-4594-bf24-561a7eb12676\"],\"1ccb61b5-022f-4735-95b4-1c57e9f7938f\":[\"215caa30-1906-4210-8294-23eb7914c1dd\",\"fa32786b-4063-4f39-b72d-a5bc0e549193\"]}}}",
                LocationTree.class);
        String ltreestr = new Gson().toJson(ltree, LocationTree.class);
        assertNotNull(ltreestr);
        LocationTree lconverted = new Gson().fromJson(ltreestr, LocationTree.class);
        assertNotNull(lconverted);
        String lconvertedstr = new Gson().toJson(lconverted, LocationTree.class);
        assertEquals(ltreestr, lconvertedstr);
    }

    @Test
    public void testGetLocationTreeOf() throws JSONException {
        String expectedCountry = "BDCountry";
        String expectedDivision = "BDDivision";
        String expectedDistrict = "BDDistrict";
        String expectedSudDistrict = "BDSubDistrict";
        String expectedUnion = "BDUnion";
        String expectedWard = "BDWard";
        JSONObject country = createLocation(expectedCountry, "");
        JSONObject division = createLocation(expectedDivision, country.getString(uuidKey));
        JSONObject district = createLocation(expectedDistrict, division.getString(uuidKey));
        JSONObject sudDistrict = createLocation(expectedSudDistrict, district.getString(uuidKey));
        JSONObject union = createLocation(expectedUnion, sudDistrict.getString(uuidKey));
        JSONObject ward = createLocation(expectedWard, union.getString(uuidKey));

        JSONObject location = new JSONObject(ls.getLocationTreeOf(expectedWard));
        deleteLocation(country.getString(uuidKey));
        JSONObject response = location.getJSONObject(locationsHierarchyKey);

        JSONObject countryJSON = response.getJSONObject(country.getString(uuidKey));
        JSONObject countryNode = countryJSON.getJSONObject(nodeKey);
        String actualParentOfCountry = countryNode.getString(parentLocationKey);
        String actualCountry = countryJSON.getString(labelKey);
        assertEquals(expectedCountry, actualCountry);
        assertEquals(null + "", actualParentOfCountry);

        JSONObject divisionJSON = countryJSON.getJSONObject(childrenKey);
        JSONObject divisionJSONOFUUID = divisionJSON.getJSONObject(division.getString(uuidKey));
        String actualDivision = divisionJSONOFUUID.getString(labelKey);
        JSONObject nodeOFDivision = divisionJSONOFUUID.getJSONObject(nodeKey);
        JSONObject parentOFDivision = nodeOFDivision.getJSONObject(parentLocationKey);
        String actualParentOFDIvision = parentOFDivision.getString(nameKey);
        assertEquals(expectedDivision, actualDivision);
        String expectedDivisionPArent = expectedCountry;
        assertEquals(expectedDivisionPArent, actualParentOFDIvision);

        JSONObject districtJSON = divisionJSONOFUUID.getJSONObject(childrenKey);
        JSONObject districtJSONOFUUID = districtJSON.getJSONObject(district.getString(uuidKey));
        String actualDistrict = districtJSONOFUUID.getString(labelKey);
        JSONObject nodeOFDistrict = districtJSONOFUUID.getJSONObject(nodeKey);
        JSONObject parentOFDistrict = nodeOFDistrict.getJSONObject(parentLocationKey);
        String actualParentOFDistrict = parentOFDistrict.getString(nameKey);
        assertEquals(expectedDistrict, actualDistrict);
        String expectedDistrictParent = expectedDivision;
        assertEquals(expectedDistrictParent, actualParentOFDistrict);

        JSONObject sudDistrictJSON = districtJSONOFUUID.getJSONObject(childrenKey);
        JSONObject subDistrictJSONOFUUID = sudDistrictJSON.getJSONObject(sudDistrict.getString(uuidKey));
        String actualSubDistrict = subDistrictJSONOFUUID.getString(labelKey);
        JSONObject nodeOFSUbDistrict = subDistrictJSONOFUUID.getJSONObject(nodeKey);
        JSONObject parentOFSubDistrict = nodeOFSUbDistrict.getJSONObject(parentLocationKey);
        String actualParentOFSubDistrict = parentOFSubDistrict.getString(nameKey);
        assertEquals(expectedSudDistrict, actualSubDistrict);
        String expectedSubDistrictParent = expectedDistrict;
        assertEquals(expectedSubDistrictParent, actualParentOFSubDistrict);

        JSONObject uionJSON = subDistrictJSONOFUUID.getJSONObject(childrenKey);
        JSONObject unionJSONOFUUID = uionJSON.getJSONObject(union.getString(uuidKey));
        String actualunion = unionJSONOFUUID.getString(labelKey);
        JSONObject nodeOFUnion = unionJSONOFUUID.getJSONObject(nodeKey);
        JSONObject parentOFUnion = nodeOFUnion.getJSONObject(parentLocationKey);
        String actualParentOFUnion = parentOFUnion.getString(nameKey);
        assertEquals(expectedUnion, actualunion);
        String expectedUnionParent = expectedSudDistrict;
        assertEquals(expectedUnionParent, actualParentOFUnion);

        JSONObject wardJSON = unionJSONOFUUID.getJSONObject(childrenKey);
        JSONObject wardJSONOFUUID = wardJSON.getJSONObject(ward.getString(uuidKey));
        String actualward = wardJSONOFUUID.getString(labelKey);
        JSONObject nodeOFWard = wardJSONOFUUID.getJSONObject(nodeKey);
        JSONObject parentOFWard = nodeOFWard.getJSONObject(parentLocationKey);
        String actualParentOFWard = parentOFWard.getString(nameKey);
        assertEquals(expectedWard, actualward);
        String expectedUnionWard = expectedUnion;
        assertEquals(expectedUnionWard, actualParentOFWard);

    }

    @Test(expected = JSONException.class)
    public void testGetLocationTreeOfZException() throws JSONException {
        String locationName = "testLocationName";
        ls.getLocationTreeOf(locationName);
    }

    @Test
    public void testGetLocation() throws JSONException {
        String locationName = "testLocationName";
        createLocation(locationName, "");
        Location location = ls.getLocation(locationName);
        String actualLocation = location.getName();
        deleteLocation(location.getLocationId());
        String expectedlocation = locationName;
        assertEquals(expectedlocation, actualLocation);
    }

    @Test
    public void testGetLocationException() throws JSONException {
        String locationName = "testLocationName";
        ls.getLocation(locationName);

    }

    @Test
    public void testGetLocationTreeOfArray() throws JSONException {
        String firstLocation = "testLocationA";
        String secondLocation = "testLocationB";
        JSONObject responseFirstLocation = createLocation(firstLocation, "");
        JSONObject responseSecondLocation = createLocation(secondLocation, "");
        String[] str = new String[2];
        str[0] = firstLocation;
        str[1] = secondLocation;
        LocationTree lt = ls.getLocationTreeOf(str);
        deleteLocation(responseFirstLocation.getString(uuidKey));
        deleteLocation(responseSecondLocation.getString(uuidKey));
        JSONObject locations = new JSONObject(lt);
        JSONObject responseLocationTree = locations.getJSONObject(locationsHierarchyKey);
        JSONObject firstLocationJSONOFUUID = responseLocationTree.getJSONObject(responseFirstLocation.getString("uuid"));
        JSONObject secondLocationJSONOFUUID = responseLocationTree.getJSONObject(responseSecondLocation.getString("uuid"));
        String actulaFirstLocation = firstLocationJSONOFUUID.getString(labelKey);
        String actulaSecondLocation = secondLocationJSONOFUUID.getString(labelKey);
        String expectedFirstLocation = firstLocation;
        String expectedSecondLocation = secondLocation;
        assertEquals(expectedFirstLocation, actulaFirstLocation);
        assertEquals(expectedSecondLocation, actulaSecondLocation);

    }

    @Test(expected = JSONException.class)
    public void testGetLocationTreeOfArrayException() throws JSONException {
        String firstLocation = "testLocationC";
        String secondLocation = "testLcationD";
        String[] str = new String[2];
        str[0] = firstLocation;
        str[1] = secondLocation;
        ls.getLocationTreeOf(str);
    }

    @Test
    public void testgetLocationTree() throws JSONException {
        String expectedLocation = "testlocationofOpenmrs";
        JSONObject responseLocation = createLocation(expectedLocation, "");
        LocationTree lt = ls.getLocationTree();
        deleteLocation(responseLocation.getString(uuidKey));
        Location location = lt.findLocation(responseLocation.getString(uuidKey));
        String actualLocation = location.getName();
        assertEquals(expectedLocation, actualLocation);

    }
}
