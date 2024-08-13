package com.cybersoft.uniclub06.security;

import com.cybersoft.uniclub06.dto.RoleDTO;
import com.cybersoft.uniclub06.request.AuthenRequest;
import com.cybersoft.uniclub06.service.AuthenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomAuthenProvider implements AuthenticationProvider {


    @Autowired
    private AuthenService authenService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();

        AuthenRequest authenRequest = new AuthenRequest(userName,password);

        List<RoleDTO> roleDtos = authenService.checkLogin(authenRequest);
        if(roleDtos.size() > 0){
            List<GrantedAuthority> authorityList = new ArrayList<>(); //Bữa sau chỉnh lại thành Stream API

            roleDtos.forEach(roleDTO -> {
                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(roleDTO.getName());
                authorityList.add(simpleGrantedAuthority);
            });

            return new UsernamePasswordAuthenticationToken("","",authorityList);
        }else{
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
