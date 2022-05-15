package io.github.olegshishkin.accounting.accounts.messages;

import io.github.olegshishkin.accounting.accounts.messages.commands.Header;

public interface Command {

  Header getHeader();
}
