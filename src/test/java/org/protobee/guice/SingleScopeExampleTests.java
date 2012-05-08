/*******************************************************************************
 * Copyright (c) 2012, Daniel Murphy and Deanna Surma
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.protobee.guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.protobee.guice.example.Battlestar;
import org.protobee.guice.example.BattlestarFactory;
import org.protobee.guice.example.scoped.CommandDeck;

import com.google.inject.ProvisionException;

public class SingleScopeExampleTests extends AbstractMultiscopeTest {

  @Test
  public void testSingleScoping() {

    BattlestarFactory factory = injector.getInstance(BattlestarFactory.class);
    Battlestar battlestar = factory.create();
    assertFalse(battlestar.isInScope());

    CommandDeck deck;
    try {
      battlestar.enterScope();
      assertTrue(battlestar.isInScope());
      assertSame(battlestar, injector.getInstance(Battlestar.class));
      deck = injector.getInstance(CommandDeck.class);
      deck.setCapacity(1);
      deck.setName("small deck");

      battlestar.exitScope();
      battlestar.enterScope();
      assertSame(deck, injector.getInstance(CommandDeck.class));
    } finally {
      battlestar.exitScope();
    }

    boolean caught = false;
    try {
      deck = injector.getInstance(CommandDeck.class);
    } catch (ProvisionException exception) {
      caught = true;
    }
    assertTrue("Deck was created outside of scope", caught);
  }

  @Test
  public void testMultipleScopeInstances() {

    BattlestarFactory factory = injector.getInstance(BattlestarFactory.class);
    Battlestar battlestar1 = factory.create();
    Battlestar battlestar2 = factory.create();

    CommandDeck deck1;
    try {
      battlestar1.enterScope();
      deck1 = injector.getInstance(CommandDeck.class);
      deck1.setCapacity(10);
      deck1.setName("Deck1");
      
    } finally {
      battlestar1.exitScope();
    }

    CommandDeck deck2;
    try {
      battlestar2.enterScope();
      deck2 = injector.getInstance(CommandDeck.class);
      deck2.setCapacity(12);
      deck2.setName("Deck2");
      assertNotSame(deck1, deck2);
    } finally {
      battlestar2.exitScope();
    }

    try {
      battlestar1.enterScope();
      CommandDeck deck = injector.getInstance(CommandDeck.class);
      assertSame(deck1, deck);
      assertEquals("Deck1", deck.getName());
    } finally {
      battlestar1.exitScope();
    }

    try {
      battlestar2.enterScope();
      CommandDeck deck = injector.getInstance(CommandDeck.class);
      assertSame(deck2, deck);
      assertEquals("Deck2", deck.getName());
    } finally {
      battlestar2.exitScope();
    }
  }
}
