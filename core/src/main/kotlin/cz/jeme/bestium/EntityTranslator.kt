package cz.jeme.bestium

import cz.jeme.bestium.api.inject.EntityInjection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.Translator
import java.text.MessageFormat
import java.util.*

object EntityTranslator : Translator {
    fun register() {
        GlobalTranslator.translator().addSource(this)
    }

    val name = BestiumPlugin.createKey("entity_translator")
    
    override fun name() = name

    override fun translate(key: String, locale: Locale): MessageFormat? = null

    override fun translate(component: TranslatableComponent, locale: Locale): Component? {
        return translations[component.key()]?.get(locale)
    }

    private val translations = hashMapOf<String, Map<Locale, Component>>()

    fun addInjection(injection: EntityInjection<*, *>) {
        translations[injection.realType.descriptionId] = injection.displayNames
    }
}