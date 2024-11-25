package com.es.sessionsecurity.service

import com.es.sessionsecurity.error.exception.BadRequestException
import com.es.sessionsecurity.error.exception.NotFoundException
import com.es.sessionsecurity.model.ROLE
import com.es.sessionsecurity.model.Session
import com.es.sessionsecurity.model.Usuario
import com.es.sessionsecurity.repository.SessionRepository
import com.es.sessionsecurity.repository.UsuarioRepository
import com.es.sessionsecurity.util.CipherUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class UsuarioService {

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository
    @Autowired
    private lateinit var sessionRepository: SessionRepository

    @Autowired
    private lateinit var cipherUtils: CipherUtils

    fun login(userLogin: Usuario) : String {

        // COMPROBACIÓN DE LOS CAMPOS DEL OBJETO USERLOGIN
        if(userLogin.password.isBlank() || userLogin.nombre.isBlank()) {
            throw BadRequestException("Los campos nombre y password son obligatorios")
        }

        // COMPROBAR CREDENCIALES
        // 1 Obtener el usuario de la base de datos
        var userBD: Usuario = usuarioRepository
            .findByNombre(userLogin.nombre)
            .orElseThrow{NotFoundException("El usuario proporcionado no existe en BDD")}

        // 2 Compruebo nombre y pass
        if(cipherUtils.decrypt(userBD.password, "nada_funcionando") == userLogin.password) {
            // 3 GENERAR EL TOKEN
            var token: String = ""
            token = cipherUtils.encrypt(userBD.nombre, "nada_funcionando")  // !!!! Aquí es importante desencriptar la password para compararla o no funcionará!!

            // 4 CREAR UNA SESSION
            val s: Session = Session(
                null,
                token,
                userBD,
                LocalDateTime.now().plusMinutes(3)
            )

            // 5 INSERTAMOS EN BDD
            sessionRepository.save(s)

            return token
        } else {
            // SI LA CONTRASEÑA NO COINCIDE, LANZAMOS EXCEPCIÓN
            throw NotFoundException("Las credenciales son incorrectas")
        }
    }

    fun insert(userRegister: Usuario): Usuario{

        when{
            userRegister.nombre.isBlank() -> throw BadRequestException("El nombre no puede estar vacío.")

            userRegister.password.isBlank() -> throw BadRequestException("La contraseña no puede estar vacía.")

            userRegister.role != ROLE.ADMIN && userRegister.role != ROLE.USER -> throw BadRequestException("El rol debe asignarse.")
        }

        val password = cipherUtils.encrypt(userRegister.password, "nada_funcionando")

        userRegister.password = password

        return usuarioRepository.save(userRegister)
    }


    fun checkForSessions(){
        //hola

    }

}