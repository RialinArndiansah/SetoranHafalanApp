package com.example.setoranhafalan.data.model

data class SetoranResponse(
    val response: Boolean,
    val message: String,
    val data: SetoranData
)

data class SetoranData(
    val info: StudentInfo,
    val setoran: SetoranDetails
)

data class StudentInfo(
    val nama: String,
    val nim: String,
    val email: String,
    val angkatan: String,
    val semester: Int,
    val dosen_pa: Dosen
)

data class Dosen(
    val nip: String,
    val nama: String,
    val email: String
)

data class SetoranDetails(
    val log: List<SetoranLog>,
    val info_dasar: InfoDasar,
    val ringkasan: List<Ringkasan>,
    val detail: List<SetoranItem>
)

data class SetoranLog(
    val id: Int,
    val keterangan: String,
    val aksi: String,
    val ip: String,
    val user_agent: String,
    val timestamp: String,
    val nim: String,
    val dosen_yang_mengesahkan: Dosen
)

data class InfoDasar(
    val total_wajib_setor: Int,
    val total_sudah_setor: Int,
    val total_belum_setor: Int,
    val persentase_progres_setor: Float,
    val tgl_terakhir_setor: String?,
    val terakhir_setor: String
)

data class Ringkasan(
    val label: String,
    val total_wajib_setor: Int,
    val total_sudah_setor: Int,
    val total_belum_setor: Int,
    val persentase_progres_setor: Float
)

data class SetoranItem(
    val id: String,
    val nama: String,
    val label: String,
    val sudah_setor: Boolean,
    val info_setoran: InfoSetoran?
)

data class InfoSetoran(
    val id: String,
    val tgl_setoran: String,
    val tgl_validasi: String,
    val dosen_yang_mengesahkan: Dosen
)