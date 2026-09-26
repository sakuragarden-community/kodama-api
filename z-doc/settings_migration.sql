-- Migrazione delle configurazioni di "config.json" nella tabella "settings".
-- Path nel formato "<section>/<group>/<field>", tutti i valori di tipo "text";
-- gli array sono salvati come stringhe con i valori separati da virgola.
-- Idempotente: i path già presenti non vengono sovrascritti.

BEGIN;

INSERT INTO settings (path, type, value)
VALUES
    -- general: configurazioni globali del progetto
    ('general/discord/guild_id',          'text', '1544701609999597615'),
    ('general/bots/disboard',             'text', '1419774015957893141'),
    ('general/colors/primary',            'text', '#F39595'),
    ('general/colors/secondary',          'text', '#FFCF69'),
    ('general/colors/blue',               'text', '#7EBDC3'),
    ('general/colors/violet',             'text', '#A68BA5'),
    ('general/colors/green',              'text', '#A8AE8D'),
    ('general/colors/snow',               'text', '#F4F2EB'),
    ('general/colors/error',              'text', '#FF5555'),
    ('general/colors/alert',              'text', '#FFDA55'),

    -- roles
    ('roles/init/role_ids',               'text', '1547269471570235392,1547269493397655583,1547269510078398554,1547269527434305626'),
    ('roles/types/master',                'text', '1547270151576231986'),
    ('roles/types/admin',                 'text', '1547266591396204554'),
    ('roles/types/moderator',             'text', '1547270190343913532'),
    ('roles/types/helper',                'text', '1547270207242764328'),
    ('roles/types/collaborator',          'text', '1547270227497324554'),
    ('roles/types/supporter',             'text', '1547270241413898360'),
    ('roles/types/bot',                   'text', '1547270259126571040'),
    ('roles/types/member',                'text', '1547270272812449874'),
    ('roles/options/disable_find_player', 'text', '1547270648009859092'),
    ('roles/interests/party_games',       'text', '1547270695350964244'),

    -- channels: canali testuali raggruppati per area
    ('channels/info/menu',                'text', '1547270829639860265'),
    ('channels/info/news',                'text', '1547270860338106460'),
    ('channels/info/server',              'text', '1547270894773211217'),
    ('channels/info/events',              'text', '1547270993603596470'),
    ('channels/info/top',                 'text', '1547271040030212186'),
    ('channels/community/main',           'text', '1547270847394484324'),
    ('channels/community/presentations',  'text', '1547270940654837840'),
    ('channels/community/new',            'text', '1547270977702854708'),
    ('channels/community/free_talk',      'text', '1547271058304798841'),
    ('channels/community/galleries',      'text', '1547271083399319642'),
    ('channels/community/promo',          'text', '1547270878289727599'),
    ('channels/gaming/find_player',       'text', '1547271118476415096'),
    ('channels/gaming/video_games',       'text', '1547271136889409576'),
    ('channels/utility/commands',         'text', '1547270916063498280'),
    ('channels/utility/support',          'text', '1547271026436603996'),

    -- voice: canali vocali generati dai trigger
    ('voice/general/category_id',         'text', '1544701610662436967'),
    ('voice/triggers/default',            'text', '1547271589148491868'),
    ('voice/triggers/nsfw',               'text', '1547271611504263240'),
    ('voice/triggers/focus',              'text', '1547271636372299826'),
    ('voice/limits/max_users',            'text', '12'),
    ('voice/limits/max_users_focus',      'text', '8')
ON CONFLICT (path) DO NOTHING;

COMMIT;
