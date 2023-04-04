package com.oneapp.oneapp.data

val highwayImages = mapOf(
    "Woodlands Checkpoint" to "https://images.data.gov.sg/api/traffic-images/2023/04/0c1e53af-1dbd-4657-b94c-39ae573141be.jpg",
    "Tuas Checkpoint" to "https://images.data.gov.sg/api/traffic-images/2023/04/34f0dbce-ce81-4be8-826c-04d6219ce754.jpg",
    "TPE" to "https://images.data.gov.sg/api/traffic-images/2023/04/910f2023-c443-4401-9623-20e00a296295.jpg",
    "SLE" to "https://images.data.gov.sg/api/traffic-images/2023/04/17c63210-6dae-4a1f-a1e0-819da907386c.jpg",
    "Sentosa Gateway" to "https://images.data.gov.sg/api/traffic-images/2023/04/fb4ba93d-5cdf-42cd-9e18-e2322bb55e8e.jpg",
    "PIE" to "https://images.data.gov.sg/api/traffic-images/2023/04/7cfe310a-6088-4bb3-a0e8-9b06d59391ca.jpg",
    "MCE" to "https://images.data.gov.sg/api/traffic-images/2023/04/6e81faa9-bc6a-44dc-bb25-bc5aa5183afb.jpg",
    "KPE" to "https://images.data.gov.sg/api/traffic-images/2023/04/e22d6a48-d7c9-4b48-b5eb-40dfd4b689ee.jpg",
    "KJE" to "https://images.data.gov.sg/api/traffic-images/2023/04/7e396658-1dce-48c5-a835-352c90add78b.jpg",
    "ECP" to "https://images.data.gov.sg/api/traffic-images/2023/04/df536872-e109-42a7-aedc-c4f4de9b05ca.jpg",
    "CTE" to "https://images.data.gov.sg/api/traffic-images/2023/04/cdc5ffbb-43d8-4bf5-9fc6-12c9359261a5.jpg",
    "BKE" to "https://images.data.gov.sg/api/traffic-images/2023/04/ffa79087-82ae-4de0-83ec-d0561168f83a.jpg",
    "AYE" to "https://images.data.gov.sg/api/traffic-images/2023/04/b34b2942-dcc3-4ae0-a07d-89dd416353a0.jpg",
    "Loyang Ave/Tanah Merah Coast Road" to "https://images.data.gov.sg/api/traffic-images/2023/04/7a4ea2ef-a9fb-4c98-ab22-8ddfb8d70bd3.jpg"
)

data class highwayMap (
    val highwayName: String,
    val camera_id: Int
)

val highwayMapList = listOf(
    highwayMap("Woodlands Checkpoint", 2702),
    highwayMap("Tuas Checkpoint", 4713),
    highwayMap("TPE", 7798),
    highwayMap("SLE", 9703),
    highwayMap("Sentosa Gateway", 4799),
    highwayMap("PIE", 6716),
    highwayMap("MCE", 1501),
    highwayMap("KPE", 1001),
    highwayMap("KJE", 8706),
    highwayMap("ECP", 3798),
    highwayMap("CTE", 1707),
    highwayMap("BKE", 2703),
    highwayMap("AYE", 4712),
    highwayMap("Loyang Ave/Tanah Merah Coast Road", 1113)
)