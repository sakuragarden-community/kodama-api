# Entità

## Member

| **Nome** | **Tipo** | **Validazione** | **Chiave** | **Valore di default** | **Descrizione** |
| --- | --- | --- | --- | --- | --- |
| id | int | valore univoco | primaria | <autogenerato> |  |
| discord_id | string | valore univoco |  |  |  |
| username | string |  |  |  |  |
| created_at | timestamp |  |  |  |  |
| joined_at | timestamp |  |  |  |  |
| left_at | timestamp |  |  |  |  |
| status | string |  |  |  |  |
| presentation | string |  |  |  |  |
| experience | int |  |  | 0 |  |

## Section

| **Nome** | **Tipo** | **Validazione** | **Chiave** | **Valore di default** | **Descrizione** |
| --- | --- | --- | --- | --- | --- |
| id | int | valore univoco | primaria | <autogenerato> |  |
| name | string |  |  |  |  |
| code | string | valore univoco |  |  |  |