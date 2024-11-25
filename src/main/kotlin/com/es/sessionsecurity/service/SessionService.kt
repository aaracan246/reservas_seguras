package com.es.sessionsecurity.service

import com.es.sessionsecurity.error.exception.BadRequestException
import com.es.sessionsecurity.model.ROLE
import com.es.sessionsecurity.model.Session
import com.es.sessionsecurity.repository.SessionRepository
import com.es.sessionsecurity.util.CipherUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SessionService {

    @Autowired
    private lateinit var cipherUtils: CipherUtils

    @Autowired
    private lateinit var sessionRepository: SessionRepository

    fun checkToken(token: String?, nombre: String) : Boolean {

        // 1º Vamos a obtener la sesión asociada al token:
        if (token == null){ throw BadRequestException("Token nulo.")}
        else{
            val tokenDescifrao = cipherUtils.decrypt(token, "nada_funcionando")


            val s: Session = sessionRepository.findByToken(tokenDescifrao).orElseThrow{ RuntimeException("Token inválido.") }

            return if(s.usuario.nombre == nombre || s.usuario.role == ROLE.ADMIN)
            // Por último, comprobamos que la fecha sea válida:
                s.fechaExp.isAfter(LocalDateTime.now())

            else{
                false
            }
        }
    }
}